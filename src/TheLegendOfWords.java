import extensions.CSVFile;
import extensions.File;

class TheLegendOfWords extends Program {

	// -- Variable globale --
	final int LONGUEUR_ECRAN = 30;
	final int HAUTEUR_ECRAN = 11;
	final int DIFF_CARA = 'a' - 'A'; // afin de passer d'un caractere majuscule a minuscule ou inversement
	final int size_ECRAN = LONGUEUR_ECRAN * 2 + 2;

	// nouveau type afin de d'acceder facilement a une couleur ou un caractere
	final Color Color = new Color();
	final Caractere Cara = new Caractere();

	// nombre d'items a posseder pour acceder a la fin
	final int boisAPosseder = 16;
	final int cordeAPosseder = 30;

	final String pressKeyToContinue = "appuyer sur une touche pour continuer";

	// -- Files / Directory --
	final String textes_Directory = "../ressources/textes/";
	final String maps_Directory = "../ressources/maps/";
	final String saves_Directory = "../ressources/saves/";
	final String arts_Directory = "../ressources/arts/";
	final String words_Directory = "../ressources/mots/";
	final String words_File = "mots.csv";

	// -- accessible et modifible partout --
	boolean fini = false;
	int LONGUEUR = 50;
	int HAUTEUR = 13;
	int currentSaveIndex; // variable afin de connaitre quelle est la sauvegarde acutelle
	boolean entreeBossDebloquer = false; //connaitre si on peut oui ou non l'entrée a la fin est débloqué
	
	Enemy[] allEnemy;
	Player player;
	String[] allMaps;
	char[][] currentWorld = new char[HAUTEUR][LONGUEUR];

	void algorithm(){
		Word[] allWords = getAllWords(); // recupere des mots d'un fichier csv ayant la traduction francaise et anglaise
		getAllMaps(); // recupere toutes les maps dans des fichiers csv dans un tableau de caractere a deux dimensions

		dialogues(size_ECRAN, getDialogueText(0)); // dialogues de debut afin d'expliquer le contexte

		saisieSaves(size_ECRAN); // demande une saise afin de connaitre quelles sauvegardes on veut choisir

		if(checkSave(currentSaveIndex)){ // verifie si la sauvegarde entree n'est pas vide
			getSave(currentSaveIndex); // si elle ne l'est pas, on charge la sauvegarde existante
		}else{
			// si la sauvegarde est vide, on initialise un nouveau joueur, un nouvelle inventaire et tous les ennemis
			initPlayer(readStringNonVide("Pseudo de votre nouveau joueur : "));
			initInventaire(player.inventaire);
			getAllEnemy();
		}
		initWorld(player.worldIndex); // on initialise le monde dans lequel le joueur est actuellement
		
		int startTime = (int)getTime(); // lorsque le jeu commence, je prend le temps initial, afin de recuperer le temps joué a la fin 
		
		do{
			clearScreen(); // saute des lignes afin d'avoir un terminal propre
			afficherInterface(); // affiche l'interface dans le terminal
			saisiePlayer(player, allEnemy, allWords); // demande la saisie du joueur
		}while(!fini && !allEnemyDead(allEnemy) && !playerDead(player));
		// la boucle se repete jusqu'a ce que le jeu n'est pas fini, ou que tous les ennemis ne sont pas mort
		// ou si le joueur n'est pas mort

		if(allEnemyDead(allEnemy)){ // verifie si tous les ennemis sont morts
			dialogues(size_ECRAN, getDialogueText(1)); // affiche un dialogue recuperer dans un txt
		}

		if(playerDead(player)){ // verifie si le joueur est mort
			dialogues(size_ECRAN, getDialogueText(2)); // affiche un dialogue recuperer dans un txt
		}
		
		//ici on calcule le temps joué avec le temps actuel, le startTime, et le temps du joueur qu'il avait ou non
		int timePlayed = (int)(getTime() - startTime)/1000 + player.timePlayed; // on divise bien par 1000 car le temps est en milliseconde
		saveGame(currentSaveIndex, timePlayed); // on sauvergarde le joueur, les ennemis, et son inventaire, dans l'emplacement de sauvegarde entree

		clearScreen(); // on clearScreen pour un terminal propre a la fin de partie
	}

	boolean allEnemyDead(Enemy[] enemies){ // fonction pour verifier que tous les ennemis sont morts
		boolean result = true;
		int i = 0;
		while(i < length(enemies)){ // on parcours tous les ennemis
			if(!enemies[i].dead){
				result = false;	// on met le resultat a faux si l'ennemi n'est pas mort
				i = length(enemies); // on stope la boucle
			}else
				i++; // si l'ennemi acutel est mort, on continue la boucle
		}
		return result; // on retourne le resultat de la fonction
	}

	String[] getDialogueText(int index){ // fonction qui renvoie une chaine de caractere depuis un fichier txt
		String directory = textes_Directory + "texte" + index + ".txt";
		File f = newFile(directory);
		String[] txt = new String[getSizeTxt(directory)]; // on initialise un tableau de String avec le nombre de ligne du fichier
		int i = 0;
		while(ready(f)){ // tant que le fichier n'a pas fini de lire
			txt[i] = readLine(f); // on met le ligne actuel dans le tableau
			i++; // on incremente afin avoir la ligne courante pour le tableau
		}

		return txt; // on retourne le resultat
	}

	int getSizeTxt(String directory){ // fonction pour connaitre le nombre de ligne d'un fichier txt
		File f = newFile(directory);
		int size = 0; // initialise la taille de size a 0
		while(ready(f)){ // tant que le fichier n'a pas fini de lire
			readLine(f); // on lit le texte afin de passer a la prochaine
			size++; //on incremente la taille de ligne
		}
		return size; // retourne le resultat
	}
	
	// fonction pour afficher un dialogue grace a une taille et un tableau de chaine de caractere
	void dialogues(int size, String[] txt){
		clearScreen();

		String curString, affichage = "";
		int x = 0;
		int ySize = 4 + length(txt);

		affichage+=menuCreateBarre(size, 0);

		for(int y = 0; y < ySize; y++){
			affichage += "" + Cara.CARA_BARRE_VERTICALE;
			x = 0;
			while(x < size){
				curString = " ";
				if(y >= 2 && y < ySize - 2){
					curString = txt[y - 2];
					if(x != size / 2 - length(curString) / 2)
						curString = " ";
				}
				if (equals(curString,"")) curString = " ";

				affichage+=curString;
				x+=length(curString);
			}
			affichage += "" + Cara.CARA_BARRE_VERTICALE + "\n";
		}

		affichage+=menuCreateBarre(size, 3);

		println(affichage);
		print(pressKeyToContinue);
		readString();
	}
	
	// Creer un nouveau joueur avec un pseudo, un temps de jeu, l'indice de map, ses positions y et x, sa vie, son level et son experience
	Player newPlayer(String pseudo, int time, int index, int y, int x, int health, int lvl, int exp){
		Player player = new Player();
		//--- Pseudo du joueur ---
		player.pseudo = pseudo;
		//--- Temps de jeu du joueur ---
		player.timePlayed = time;
		//--- Position et monde ---
		player.worldIndex = index;
		player.posY = y;
		player.posX = x;
		//--- Vie ---
		player.health = health;
		//--- Level et experience ---
		player.level = lvl;
		player.experience = exp;
		//--- inventaire ---
		player.inventaire = new Item[2];
		return player;
	}
	
	// Creer un nouvel ennemi avec un indice pour l'ascii art, son incide de map, ses positions y et x, et son item jeté à sa mort
	Enemy newEnemy(int indexArt, int worldIndex, int y, int x, Item itemToDrop){
		Enemy enemy = new Enemy();
		enemy.dead = false;
		enemy.indexArt=  indexArt;
		enemy.worldIndex = worldIndex;
		enemy.posY = y;
		enemy.posX = x;
		enemy.itemToDrop = itemToDrop;
		return enemy;
	}

	// Creer un nouveau mot avec sa traduction francaise et anglaise
	Word newWord(String en, String fr){
		Word word = new Word();
		word.motEn = en;
		word.motFr = fr;
		return word;
	}

	// Creer une nouvelle lettre avec un caractere, et une visibilité a false
	Lettre newLettre(char c){
		Lettre l = new Lettre();
		l.caractere = c;
		l.visible = false;
		return l;
	}

	// Creer un nouvel item avec son type d'item (Vide, Bois, Corde) et sa quantité
	Item newItem(ItemType type, int quantity){
		Item item = new Item();
		item.type = type;
		item.quantity = quantity;
		return item;
	}

	// fonction qui permet d'initialiser l'inventaire d'items qui ont le type Vide
	void initInventaire(Item[] content){
		for(int i = 0; i < length(content); i++)
			content[i] = newItem(ItemType.Vide, 0);
	}

	// fonction qui un nouveau joueur avec un pseudo
	void initPlayer(String pseudo){
		player = newPlayer(pseudo, 0, 0, HAUTEUR/2, 6, getMaxHealth(), 1, 0);
	}
	
	// fonction qui demande au joueur de choisir un indice pour la sauvegarde
	void saisieSaves(int size){
		clearScreen();
		println(afficherSaves(size));
		String response;
		int currentIndex = 0;
		do{
			print("Quelle sauvegarde voulez vous choisir : ");
			response = readString();
			if(length(response) > 0 && estChiffre(charAt(response, 0))){
				currentIndex = charAt(response, 0) - '0';
			}

			if(!estChiffreValide(currentIndex)){
				println("Erreur : veuillez reessayez");
			}
		}while(!estChiffreValide(currentIndex));

		currentSaveIndex = currentIndex;
	}

	// Demande si le caractere entre est un chiffre ou non
	boolean estChiffre(char cara){
		return cara >= '0' && cara <= '9';
	}

	// Demande si le chiffre/nombre entre est valide (pour le jeu actuel, donc entre 1 et 3)
	boolean estChiffreValide(int nmb){
		return nmb >= 1 && nmb <= 3;
	}

	// fonction qui demande a l'utilisateur d'entre une chaine de caractere non vide sinon affiche un message en boucle
	String readStringNonVide(String message){
		String txt = "";
		do{
			if (message != "") print(message);
			txt = readString();
		}while(length(txt)<1);
		return txt;
	}
	
	// fonction qui permet de sauvegarder la partie
	void saveGame(int index, int time){
		savePlayer(index, time);
		saveInventory(index);
		saveEnemy(index);
	}
	
	// fonction qui recupere les sauvegardes des ennemis, du joueur et de l'inventaire (si disponible)
	void getSave(int index){
		if (checkSave(index, "savePlayer.csv") && checkSave(index, "saveInventory.csv")){
			getSavePlayer(index);
			getSaveInventory(index);
		}
		else{
			initPlayer("joueur");
			initInventaire(player.inventaire);
		}

		if (checkSave(index, "saveEnemy.csv")){
			getSaveEnemy(index);
		}
		else{
			getAllEnemy();
		}
	}

	// fonction qui sauvegarde le joueur (toute ses variables), dans un fichier csv
	void savePlayer(int index, int time){
		String directory = saves_Directory + "save-" + index + "/savePlayer.csv";
		String[][] content = new String[8][2];

		content[0][0] = "Pseudo";
			content[0][1] = player.pseudo;
		content[1][0] = "Time";
			content[1][1] = "" + time;
		content[2][0] = "WorldIndex";
			content[2][1] = "" + player.worldIndex;
		content[3][0] = "position Y";
			content[3][1] = "" + player.posY;
		content[4][0] = "position X";
			content[4][1] = "" + player.posX;
		content[5][0] = "Health";
			content[5][1] = "" + player.health;
		content[6][0] = "Level";
			content[6][1] = "" + player.level;
		content[7][0] = "Experience";
			content[7][1] = "" + player.experience;

		saveCSV(content, directory);
	}

	// fonction qui sauvegarde l'inventaire (son type et sa quantité) dans un fichier csv
	void saveInventory(int index){
		String directory = saves_Directory + "save-" + index + "/saveInventory.csv";
		String[][] content = new String[7][3];

		content[0][0] = "Slot";
			content[0][1] = "" + "Item";
				content[0][2] = "" + "Quantity";

		for(int i = 0; i < length(player.inventaire); i++){
			content[i+1][0] = "" + (i+1);
				content[i+1][1] = "" + player.inventaire[i].type.name();
					content[i+1][2] = "" + player.inventaire[i].quantity;
		}

		saveCSV(content, directory);
	}

	// fonction qui sauvegarde les ennemis (rtoutes ses variables) en vie seulement dans un fichier csv
	void saveEnemy(int index){
		String directory = saves_Directory + "save-" + index + "/saveEnemy.csv";
		int sizeEnemyAlive = 0;
		for(int i = 0; i < length(allEnemy); i++){
			if(!allEnemy[i].dead)
				sizeEnemyAlive++;
		}

		String[][] content = new String[sizeEnemyAlive][6];
		int sizeEnemy = 0;

		for(int i = 0; i < length(allEnemy); i++){
			if(!allEnemy[i].dead){
				content[sizeEnemy][0] = "Enemy:" + (sizeEnemy+1);
				content[sizeEnemy][1] = "" + allEnemy[i].indexArt;
				content[sizeEnemy][2] = "" + allEnemy[i].worldIndex;
				content[sizeEnemy][3] = "" + allEnemy[i].posY;
				content[sizeEnemy][4] = "" + allEnemy[i].posX;
				content[sizeEnemy][5] = allEnemy[i].itemToDrop.type.name() + ";" + allEnemy[i].itemToDrop.quantity;
				
				sizeEnemy++;
			}
		}

		saveCSV(content, directory);
	}

	// fonction qui verifie si le dossier de sauvegarde actuellement utilisé n'est pas vide
	boolean checkSave(int index){
		String directory = saves_Directory + "save-" + index + "/";
		return length(getAllFilesFromDirectory(directory)) > 0;
	}

	// fonction qui verifie sur le fichier csv existe dans le dossier de sauvegarde actuellement utilisé
	boolean checkSave(int index, String fileName){
		String directory = saves_Directory + "save-" + index + "/";
		String[] files = getAllFilesFromDirectory(directory);
		boolean resultat = false;
		int i = 0;
		while(i < length(files)){
			if(equals(files[i],fileName)){
				resultat = true;
				i = length(files);
			}else
				i++;
		}
		return resultat;
	}

	// verifie que les trois fichiers sont present dans le dossier de sauvegarde actuellement utilisé
	boolean stateSave(int index){
		String[] files = getAllFilesFromDirectory(saves_Directory + "/save-" + index);
		String fileNamePlayer = "savePlayer.csv";
		String fileNameInventory = "saveInventory.csv";
		String fileNameEnemy = "saveEnemy.csv";

		return tabContainString(files, fileNamePlayer) && tabContainString(files, fileNameInventory) && tabContainString(files, fileNameEnemy);
	}

	// fonction qui recupere la sauvegarde du joueur dans le dossier de sauvegarde actuellement utilisé
	void getSavePlayer(int index){
		String directory = saves_Directory + "save-" + index + "/savePlayer.csv";
		CSVFile file = loadCSV(directory);

		String pseudo = getCell(file, 0, 1);
		int timePlayed = stringToInt(getCell(file, 1, 1));
		int worldIndex = stringToInt(getCell(file, 2, 1));
		int posY = stringToInt(getCell(file, 3, 1));
		int posX = stringToInt(getCell(file, 4, 1));
		int health = stringToInt(getCell(file, 5, 1));
		int level = stringToInt(getCell(file, 6, 1));
		int experience = stringToInt(getCell(file, 7, 1));
		
		player = newPlayer(pseudo, timePlayed, worldIndex, posY, posX, health, level, experience);
	}

	// fonction qui recupere la sauvegarde de l'inventaire dans le dossier de sauvegarde actuellement utilisé
	void getSaveInventory(int index){
		String directory = saves_Directory + "save-" + index + "/saveInventory.csv";
		CSVFile file = loadCSV(directory);

		String currentType;
		int currentQuantity;

		for(int i = 0; i < length(player.inventaire); i++){
			currentType = getCell(file, i+1, 1);
			currentQuantity = stringToInt(getCell(file, i+1, 2));
			player.inventaire[i] = newItem(getTypeByName(currentType), currentQuantity);
		}
	}

	// fonction qui recupere la sauvegarde des ennemis dans le dossier de sauvegarde actuellement utilisé
	void getSaveEnemy(int index){
		String directory = saves_Directory + "save-" + index + "/saveEnemy.csv";
		CSVFile file = loadCSV(directory);
		int nbEnemy = rowCount(file);

		int curArtIndex, worldIndex, posY, posX, curItemQuantity;
		String curItemTxt;
		ItemType curItemType;

		allEnemy = new Enemy[nbEnemy];
		for(int i = 0; i < nbEnemy; i++){
			curArtIndex = stringToInt(getCell(file, i, 1));
			worldIndex = stringToInt(getCell(file, i, 2));
			posY = stringToInt(getCell(file, i, 3));
			posX = stringToInt(getCell(file, i, 4));
			
			curItemTxt = getCell(file, i, 5);
			curItemType = getTypeByName(substring(curItemTxt, 0 ,getSeparator(curItemTxt, ';')));
			curItemQuantity = stringToInt(substring(curItemTxt, getSeparator(curItemTxt, ';') + 1, length(curItemTxt)));

			allEnemy[i] = newEnemy(curArtIndex, worldIndex, posY, posX, newItem(curItemType, curItemQuantity));
		}
	}

	// fonction qui retourne getSeparator() sans avoir a preciser l'indice de depart
	int getSeparator(String txt, char c){
		return getSeparator(txt, c, 0);
	}

	// fonction recursive qui va retourner l'indice du caractere dans la chaine de caractere
	int getSeparator(String txt, char c, int i){
		if(length(txt) == 0){
			return -1;
		}
		if(charAt(txt, 0) == c){
			return i;
		}
		return getSeparator(substring(txt, 1, length(txt)), c, i+1);
	}

	// fonction qui retourne un itemType (enum) grace au nom du type
	ItemType getTypeByName(String name){
		if(equals(ItemType.Vide.name() + "", name)) return ItemType.Vide;
		else if(equals(ItemType.Bois.name() + "", name)) return ItemType.Bois;
		else if(equals(ItemType.Corde.name() + "", name)) return ItemType.Corde;
		else return ItemType.Vide;
	}

	// recupere tous les mots d'un fichier csv dans le format <motAnglais,motFrancais>
	Word[] getAllWords(){
		CSVFile file = loadCSV(words_Directory + words_File);
		Word[] words = new Word[rowCount(file)];

		for(int y = 0; y < length(words);y++){
			words[y] = newWord(getCell(file, y, 0), getCell(file, y, 1));
		}

		return words;
	}

	// recupere tous les chemins(jusqu'au csv) des map (map constitué uniquement de caracteres)
	void getAllMaps(){
		String directrory = maps_Directory, currentFile;
		String[] maps_file = getAllFilesFromDirectory(directrory);
		allMaps = new String[length(maps_file)];
		for(int i = 0; i < length(maps_file);i++){
			currentFile = maps_file[i];
			if(length(currentFile) >= 4 && equals(".csv", substring(currentFile, length(currentFile)-4, length(currentFile)))){
				allMaps[charAt(currentFile, 3) - '0'] = currentFile;
			}
		}
	}

	// recupere tous les ennemis present au debut d'une partie
	void getAllEnemy(){
		allEnemy = new Enemy[]{
			//ennemis de test
			newEnemy(0, 0, 2, 3, newItem(ItemType.Corde, 1)),
			newEnemy(1, 0, 2, 6, newItem(ItemType.Bois, 1)),
			newEnemy(2, 0, 2, 9, newItem(ItemType.Corde, 1)),

			//map 0
			newEnemy(0, 0, 6, 22, newItem(ItemType.Bois, 1)),
			newEnemy(1, 0, 6, 45, newItem(ItemType.Corde, 4)),
			//map 1
			newEnemy(2, 1, 5, 10, newItem(ItemType.Bois, 2)),
			//map 2
			newEnemy(3, 2, 8, 25, newItem(ItemType.Corde, 6)),
			newEnemy(4, 2, 4, 4, newItem(ItemType.Bois, 3)),
			//map 3
			newEnemy(5, 3, 7, 22, newItem(ItemType.Corde, 8)),
			newEnemy(6, 3, 1, 4, newItem(ItemType.Bois, 4)),
			//map 4
			newEnemy(7, 4, 4, 15, newItem(ItemType.Corde, 10)),
			newEnemy(8, 4, 4, 22, newItem(ItemType.Bois, 5)),
			//map 5 - BOSS
			newEnemy(9, 5, 15, 14, newItem(ItemType.Bois, 0))
		};
	}

	// demande la saisie de l'utilisateur pour connaitre ce qu'il veut effectué (se mouvoir dans la map, ou sauvegarder)
	void saisiePlayer(Player player, Enemy[] enemies, Word[] words){
		String chxMov;
		char curCara = ' ';
		do{
			chxMov = readStringNonVide("Choix : ");
			curCara = charAt(chxMov, 0);
		}while(curCara != Cara.INPUT_RIGHT &&
		curCara != Cara.INPUT_LEFT &&
		curCara != Cara.INPUT_UP &&
		curCara != Cara.INPUT_DOWN &&
		curCara != Cara.INPUT_EXIT);

		if(curCara == Cara.INPUT_EXIT) fini = true;
		if(curCara == Cara.INPUT_RIGHT && positionValide(currentWorld, player.posX+1, player.posY)) player.posX++;
		if(curCara == Cara.INPUT_LEFT && positionValide(currentWorld, player.posX-1, player.posY)) player.posX--;
		if(curCara == Cara.INPUT_DOWN && positionValide(currentWorld, player.posX, player.posY+1)) player.posY++;
		if(curCara  == Cara.INPUT_UP && positionValide(currentWorld, player.posX, player.posY-1)) player.posY--;

		checkInteractMap(player, enemies, words);
	}

	// verifie si il y a une interaction a la position ou se trouve le joueur actuellement
	void checkInteractMap(Player player, Enemy[] enemies, Word[] words){
		if(currentWorld[player.posY][player.posX] == Cara.CARA_MAP0) changeMap(player, 0);
		else if(currentWorld[player.posY][player.posX] == Cara.CARA_MAP1) changeMap(player, 1);
		else if(currentWorld[player.posY][player.posX] == Cara.CARA_MAP2) changeMap(player, 2);
		else if(currentWorld[player.posY][player.posX] == Cara.CARA_MAP3) changeMap(player, 3);
		else if(currentWorld[player.posY][player.posX] == Cara.CARA_MAP4) changeMap(player, 4);
		else if(currentWorld[player.posY][player.posX] == Cara.CARA_MAP5){
			if(checkItemAvance(player.inventaire) || entreeBossDebloquer){
				if(!entreeBossDebloquer){
					entreeBossDebloquer = true;
					useItemToAvance(player.inventaire);
					dialogues(size_ECRAN, getDialogueText(4));
				}
				changeMap(player, 5);
			}else{
				dialogues(size_ECRAN, getDialogueText(3));
			}
		}
		
		checkInteractMapAround(enemies, words, Cara.CARA_ENEMY, 1);
	}

	// verifie si il y a une interaction autour de la position ou se trouve le joueur actuellement
	void checkInteractMapAround(Enemy[] enemies, Word[] words, char CARA, int distance){
		for(int y = - distance; y <= distance; y++){
			for(int x = - distance; x <= distance; x++){
				if(player.posY + y < HAUTEUR && player.posY + y >= 0 && player.posX + x < LONGUEUR && player.posX + x >= 0){
					if(currentWorld[player.posY + y][player.posX + x] == CARA || checkEnemy(player.worldIndex, player.posY + y, player.posX + x)){
						startAttack(words, getEnemyInPos(enemies, player.worldIndex, player.posY + y, player.posX + x));
					}
				}
			}
		}
	}

	// retourne l'ennemi se trouvant a la position demandé
	Enemy getEnemyInPos(Enemy[] enemies, int worldIndex, int y, int x){
		Enemy enemy = new Enemy();
		int i = 0;
		while(i < length(enemies)){
			if(checkPos(enemies[i], worldIndex, y, x)){
				enemy = enemies[i];
				i = length(enemies);
			}else
				i++;
		}
		return enemy;
	}

	// initialise la nouvelle map, et initialise également le joueur par rapport a la map precedente
	void changeMap(Player player, int newIndex){
		initWorld(newIndex);
		if(player.worldIndex == 0 && newIndex == 1) initPlayerPosition(player, HAUTEUR/2, 1);
		else if(player.worldIndex == 1 && newIndex == 0) initPlayerPosition(player, HAUTEUR/2, LONGUEUR-2);
		else if(player.worldIndex == 1 && newIndex == 2) initPlayerPosition(player, HAUTEUR-2,LONGUEUR/2+1);
		else if(player.worldIndex == 1 && newIndex == 3) initPlayerPosition(player, 1, LONGUEUR/2-1);
		else if(player.worldIndex == 1 && newIndex == 4) initPlayerPosition(player, HAUTEUR/2, 1);
		else if(player.worldIndex == 2 && newIndex == 1) initPlayerPosition(player, 1, LONGUEUR/2+1);
		else if(player.worldIndex == 3 && newIndex == 1) initPlayerPosition(player, HAUTEUR-2, LONGUEUR/2+1);
		else if(player.worldIndex == 4 && newIndex == 5) initPlayerPosition(player, HAUTEUR/2, 1);
		else if(player.worldIndex == 4 && newIndex == 1) initPlayerPosition(player, HAUTEUR/2, LONGUEUR-2);
		else if(player.worldIndex == 5 && newIndex == 4) initPlayerPosition(player, HAUTEUR/2, LONGUEUR-2);
		else initPlayerPosition(player, HAUTEUR/2, LONGUEUR/2);
		player.worldIndex = newIndex;
	}

	// verifie si la nouvelle position sur la map est possible ou non
	boolean positionValide(char[][] currentWorld, int newPosX, int newPosY){

		if(newPosX < 0 || newPosX >= LONGUEUR) return false;
		if(newPosY < 0 || newPosY >= HAUTEUR) return false;
		if(currentWorld[newPosY][newPosX] == Cara.CARA_EAU) return false;
		if(currentWorld[newPosY][newPosX] == Cara.CARA_MAP5 &&
		!checkItemAvance(player.inventaire) && !entreeBossDebloquer) return false;

		return true;
	}

	// initialise la nouvelle position du joueur
	void initPlayerPosition(Player player, int posY, int posX){
		player.posY = posY;
		player.posX = posX;
	}

	// initialise la nouvelle map grace au chemin et a l'indice, il met également a jour la HAUTEUR, et la LARGEUR de la map
	void initWorld(int currentIndexWorld){
		CSVFile file = loadCSV(allMaps[currentIndexWorld]);
		HAUTEUR = rowCount(file);
		LONGUEUR = columnCount(file);
		currentWorld = new char[HAUTEUR][LONGUEUR];
		char currentCara;

		for(int y = 0; y < HAUTEUR;y++){
			for(int x = 0; x < LONGUEUR;x++){
				String currentTxt = getCell(file, y, x);
				if (length(currentTxt) > 0){
					currentCara = charAt(currentTxt, 0);
					if(currentCara == Cara.CARA_HERBE && random() < 0.04) currentCara = Cara.CARA_FLOWER;
					if(currentCara == Cara.CARA_BRIDGE && random() < 0.15) currentCara = Cara.CARA_CRACK;
					currentWorld[y][x] = currentCara;
				}
			}
		}
	}
	
	// initialise un tableau de lettres qui constitue le mot demandé
	void initMots(Lettre[] lettres, String word){
		char charFirstVisibleWord = charAt(word, (int)(length(word)*random()));
		for(int i = 0; i < length(lettres); i++)
			lettres[i] = newLettre(charAt(word, i));
		decouvreLettre(lettres, charFirstVisibleWord);
	}

	// cette fonction retourne grace a un caratere une couleur (avec pour certain, la couleur ET un caractere) afin d'afficher la map
	String modifCharToColor(char caractere){
		String currentCaractere = "  ";
		String currentColor = "";

		if(caractere == Cara.CARA_PLAYER)
		{
			currentColor = Color.COLOR_PURPLE;
			currentCaractere = "^^";
		}
		else if(caractere == Cara.CARA_ENEMY)
		{
			currentColor = Color.COLOR_BLACK;
			currentCaractere = "**";
		}
		else if (caractere == Cara.CARA_FISH){
			currentColor = Color.COLOR_BLUE;
			currentCaractere = Cara.FISH + " ";
		}
		else if(caractere == Cara.CARA_EAU){
			currentColor = Color.COLOR_BLUE;
			if (random() < 0.025) currentCaractere = Cara.WAVE + " ";
		}
		else if(caractere == Cara.CARA_FLOWER){
			currentColor = Color.COLOR_GREEN;
			currentCaractere = Cara.FLOWER + " ";
		}
		else if(caractere == Cara.CARA_CRACK){
			currentColor = Color.COLOR_YELLOW;
			currentCaractere = Cara.CRACK + " ";
		}
		else if(caractere == Cara.CARA_HERBE) currentColor = Color.COLOR_GREEN;
		else if(caractere == Cara.CARA_MAP0) currentColor = Color.COLOR_RED;
		else if(caractere == Cara.CARA_MAP1) currentColor = Color.COLOR_RED;
		else if(caractere == Cara.CARA_MAP2) currentColor = Color.COLOR_RED;
		else if(caractere == Cara.CARA_MAP3) currentColor = Color.COLOR_RED;
		else if(caractere == Cara.CARA_MAP4) currentColor = Color.COLOR_RED;
		else if(caractere == Cara.CARA_MAP5) currentColor = Color.COLOR_RED;
		else if(caractere == Cara.CARA_BRIDGE) currentColor = Color.COLOR_YELLOW;
		else if(caractere == Cara.CARA_PLATFORM) currentColor = Color.COLOR_WHITE;

		return currentColor + currentCaractere + Color.COLOR_RESET;
	}

	// cette fonction retourne une chaine de caractere en la couleur souhaité
	String setStringColor(String txt, String color){
		return color + txt + Color.COLOR_RESET;
	}
	
	// verifie si a la position donnée il y a un ennemi en vie
	boolean checkEnemy(int worldIndex, int posY, int posX){
		boolean valide = false;
		int i = 0;
		while(i < length(allEnemy)){
			if(checkPos(allEnemy[i], worldIndex, posY, posX) && !allEnemy[i].dead){
				valide = true;
				i = length(allEnemy);
			}else
				i++;
		}
		return valide;
	}
	
	// verifie si l'ennemi est bien sur la map et la position donnée
	boolean checkPos(Enemy enemy, int worldIndex, int y, int x){
		return enemy.worldIndex == worldIndex && enemy.posY == y && enemy.posX == x;
	}

	// affiche l'interface pour l'utilisateur
	void afficherInterface(){
		String allInterface = "";
		allInterface += menuCreateBarre(size_ECRAN, 0) + afficherWorld(size_ECRAN);
		allInterface += menuCreateBarre(size_ECRAN, 1) + afficherStats(size_ECRAN, player);
		allInterface += menuCreateBarre(size_ECRAN, 1) + afficherInventaire(size_ECRAN, player);
		allInterface += menuCreateBarre(size_ECRAN, 1) + afficherMenu(size_ECRAN);
		allInterface += menuCreateBarre(size_ECRAN, 3);
		println(allInterface);
	}

	// retourne la map en remplacant les caracteres par des couleurs
	String afficherWorld(int size){
		String curString, map = "";
		int curSize, y_START = 0, x_START = 0, y_END, x_END, reelX;

		map+=menuCreateTitle(size, "Monde");

		if(LONGUEUR_ECRAN < length(currentWorld,2) && player.posX > LONGUEUR_ECRAN/2){
			if(player.posX >= LONGUEUR - LONGUEUR_ECRAN/2)
				x_START = LONGUEUR - LONGUEUR_ECRAN;
			else
				x_START = player.posX - LONGUEUR_ECRAN/2;
		}
		else x_START = 0;
		
		if(HAUTEUR_ECRAN < length(currentWorld,1) && player.posY > HAUTEUR_ECRAN/2){
			if(player.posY >= HAUTEUR - HAUTEUR_ECRAN/2)
				y_START = HAUTEUR - HAUTEUR_ECRAN;
			else
				y_START = player.posY - HAUTEUR_ECRAN/2;
		}
		else y_START = 0;

		y_END = y_START + HAUTEUR_ECRAN;
		x_END = x_START + LONGUEUR_ECRAN;
		
		for(int y = y_START; y < length(currentWorld, 1) && y < y_END; y++){
			map += "" + Cara.CARA_BARRE_VERTICALE + Cara.CARA_ESPACE;
			for(int i = 0; i < LONGUEUR_ECRAN/2 - length(currentWorld, 2)/2; i++) map+="  ";
			for(int x = x_START; x < length(currentWorld, 2) && x < x_END; x++){
				if(y == player.posY && x == player.posX)
					map += modifCharToColor(Cara.CARA_PLAYER);
				else if (checkEnemy(player.worldIndex, y, x))
					map += modifCharToColor(Cara.CARA_ENEMY);
				else
					map += modifCharToColor(currentWorld[y][x]);
			}
			if (length(currentWorld, 2)%2 == 0){
				for(int i = 0; i < LONGUEUR_ECRAN/2 - length(currentWorld, 2)/2; i++) map+="  ";
			}else{
				for(int i = 0; i < LONGUEUR_ECRAN/2 - length(currentWorld, 2)/2 - 1; i++) map+="  ";
			}
			map += "" + Cara.CARA_ESPACE + Cara.CARA_BARRE_VERTICALE + "\n";
		}
		
		return map;
	}

	// retourne un interface contenant les statistiques du joueur
	String afficherStats(int size, Player player){
		String curColor,curString,affichage = "";
		String txtPlayer, txtLevel, txtHealth, txtExp;
		int x = 0, iToAdd, txtBarSize = size/5;

		txtExp = getBarProgress(Color.COLOR_FG_CYAN, txtBarSize, player.experience, getMaxExperience(player.level));
		txtHealth = getBarProgress(Color.COLOR_FG_RED, txtBarSize, player.health, getMaxHealth());

		txtHealth = "HP " + addZeroToNumber(player.health, 3) + " " + txtHealth;
		txtExp  = "XP "+  addZeroToNumber(player.experience, 3) + " " + txtExp;
		txtPlayer = "PL     " + getNamePlayerWithSize(player.pseudo, txtBarSize) + "";
		txtLevel = "LV " + addZeroToNumber(player.level, 3);
		txtBarSize += 7;
		
		affichage+=menuCreateTitle(size, "Statistiques");

		for(int y = 0; y < 2; y++){
			affichage+=Cara.CARA_BARRE_VERTICALE;
			x = 0;
			while(x < size){
				curString = " ";
				iToAdd = 1;
				if (y == 0 && x == (int)(size*0.25-txtBarSize/2)){
					curString = txtPlayer;
					iToAdd = length(txtPlayer);
				}
				else if (y == 0 && x == (int)(size*0.75-txtBarSize/2)){
					curString = txtLevel;
					iToAdd = length(txtLevel);
				}
				else if (y == 1 && x == (int)(size*0.25-txtBarSize/2)){
					curString = txtHealth;
					iToAdd = txtBarSize;
				}
				else if (y == 1 && x == (int)(size*0.75-txtBarSize/2)){
					curString = txtExp;
					iToAdd = txtBarSize;
				}

				affichage += curString;
				x+=iToAdd;
			}
			affichage+=Cara.CARA_BARRE_VERTICALE + "\n";
		}
		return affichage;
	}

	//retourne une barre de progression avec la couleur demandé, sa taille, et la progression de la barre
	String getBarProgress(String color, int size, int current, int max){
		String curColor, txt = "";
		int x = 0;
		while(x < size){
			if(current * size / max > x)
				curColor = color;
			else
				curColor = Color.COLOR_FG_BLACK;
			txt += setStringColor("" + Cara.CARA_PROGRESS, curColor);
			x++;
		}
		return txt;
	}

	// retourne une chaine de caractere afin de mettre des '0' devant un nombre, par exemple addZeroToNumber(5,3) => 005
	String addZeroToNumber(int number, int nbZero){
		int size = length(number + "");
		String txt = "";
		for(int i = 0; i < nbZero - size; i++) txt+="0";
		txt+=number;
		if(length(txt) > nbZero){
			txt = "";
			for(int i = 0; i < nbZero; i++){
				txt+="9";
			}
		}
		return txt;
	}

	// retourne un interface contenant l'inventaire du joueur
	String afficherInventaire(int size, Player player){
		String curString, txt = "";
		Item currentItem;
		int x = 0;

		txt+=menuCreateTitle(size, "Inventaire");

		txt+=Cara.CARA_BARRE_VERTICALE;
		while(x < size){
			curString = " ";
			if(x == (int)(size * 0.25) - (7 + length(player.inventaire[0].type.name()))/2){
				currentItem = player.inventaire[0];
				curString = "1. " + addZeroToNumber(currentItem.quantity, 2) + "x " + currentItem.type.name();
			}
			if(x == (int)(size * 0.75) - (7 + length(player.inventaire[1].type.name()))/2){
				currentItem = player.inventaire[1];
				curString = "2. " + addZeroToNumber(currentItem.quantity, 2) + "x " + currentItem.type.name();
			}
			txt+=curString;
			x+=length(curString);
		}
		txt+=Cara.CARA_BARRE_VERTICALE+"\n";

		return txt;
	}

	// retourne un interface contenant le choix des differentes sauvegardes (de 1 à 3)
	String afficherSaves(int size){
		String[] currentFiles, files = getAllFilesFromDirectory(saves_Directory);
		String curString, txt = "";
		int saveIndex, Xindex, xToAdd, x = 0;
		String newGame = "Nouvelle partie";
		String loadGame = "";
		String timePlayed = "";
		int sizeTime = length("00:00:00");

		txt+=menuCreateBarre(size, 0);
		txt+=menuCreateTitle(size, "Save");
		txt+=menuCreateBarre(size, 7);

		for(int y = 0; y < 7; y++){
			txt+="" + Cara.CARA_BARRE_VERTICALE;
			x = 0;
			while(x < size){
				saveIndex = (x * 3) / size + 1;
				curString = " ";
				xToAdd=1;
				if(stateSave(saveIndex)){
					loadGame = getInfoPlayerFromSave(saveIndex, size/3-9);
					timePlayed = getTimePlayerFromSave(saveIndex);
					Xindex = length(loadGame)/2;
				}else{
					Xindex = length(newGame)/2;
				}
				
				if(x == (int)(size * ((double)1/(double)3)) || x == (int)(size * ((double)2/(double)3))){
					curString = "" + Cara.CARA_BARRE_VERTICALE;
				}
				
				if(y == 2 && (x == (int)(size * (1.0/6.0) - 3) || x == (int)(size * 0.5 - 3) || x == (int)(size * (5.0/6.0) - 2))){
					curString="-- " + saveIndex + " --";
					xToAdd=7;
				}
				
				if(x == (int)(size * (1.0/6.0)) - Xindex || x == (int)(size * 0.5) - Xindex || x == (int)(size * (5.0/6.0)) - Xindex){
					if(y == 4){
						if(stateSave(saveIndex)){
							curString = setStringColor(loadGame, Color.COLOR_FG_GREEN);
							xToAdd = length(loadGame);
						}else{
							curString = setStringColor(newGame, Color.COLOR_FG_RED);
							xToAdd = length(newGame);
						}
					}
				}
				
				
				if(y == 5 && stateSave(saveIndex) && 
				(x == (int)(size * (1.0/6.0)) - sizeTime/2 ||
				x == (int)(size * 0.5) - sizeTime/2 ||
				x == (int)(size * (5.0/6.0)) - sizeTime/2)){
					curString = setStringColor(timePlayed, Color.COLOR_FG_CYAN);
					xToAdd = length(timePlayed);
				}

				txt+=curString;
				x+=xToAdd;
			}
			txt+="" + Cara.CARA_BARRE_VERTICALE + "\n";
		}
		txt+=menuCreateBarre(size, 8);
		return txt;
	}

	// retourne le temps de jeu de la sauvegarde sous le format (00:00:00)
	String getTimePlayerFromSave(int index){
		CSVFile file = loadCSV(saves_Directory + "/save-" + index + "/savePlayer.csv");
		int time = stringToInt(getCell(file, 1, 1));
		int heures = time/3600;
		time-=heures*3600;
		int minutes = time/60;
		time-=minutes*60;
		int secondes = time;
		return addZeroToNumber(heures, 2) + ":" + addZeroToNumber(minutes, 2) + ":" + addZeroToNumber(secondes, 2);
	}

	// retourne les informations importante d'une sauvegarde sous le format (nom - LV : 000)
	String getInfoPlayerFromSave(int index, int usernameSizeMax){
		CSVFile file = loadCSV(saves_Directory + "/save-" + index + "/savePlayer.csv");
		String username = getCell(file, 0, 1);
		int level = stringToInt(getCell(file, 6, 1));
		return getNamePlayerWithSize(username, usernameSizeMax) + " - LV:" + level;
	}

	// retourne le nom du joueur avec une taille maximale sinon la fonction remplace par des "..."
	String getNamePlayerWithSize(String txt, int maxSize){
		if(length(txt) > maxSize)
			txt = substring(txt, 0, maxSize-3) + "...";
		return txt;
	}

	// verifie si une chaine de caractere est present dans un tableau de chaine de caractere
	boolean tabContainString(String[] tab, String txt){
		boolean valide = false;
		int i = 0;

		while(i < length(tab)){
			if(equals(tab[i],txt)){
				valide = true;
				i = length(tab);
			}else
				i++;
		}
		return valide;
	}
	
	// retourne une interface contenant les touches a utilisés
	String afficherMenu(int size){
		String touchLEFT = Cara.INPUT_LEFT + " : gauche";
		String touchRIGHT = Cara.INPUT_RIGHT+  " : droite";
		String touchUP = Cara.INPUT_UP + " : haut";
		String touchDOWN = Cara.INPUT_DOWN + " : bas";
		
		String touchEXIT = Cara.INPUT_EXIT + " :";
		String exitAndSave = "quitter et sauvergarder";

		String curString, menu = "";
		
		int x = 0;
		for(int y = 0; y < 6;y++){
			menu += Cara.CARA_BARRE_VERTICALE;
			x = 0;
			while(x < size){
				curString = "" + Cara.CARA_ESPACE;
				if(x == (int)(size * 0.15)){
					if(y == 1) curString = touchLEFT;
					else if(y == 2) curString = touchRIGHT;
					else if(y == 3) curString = touchUP;
					else if(y == 4) curString = touchDOWN;
				}

				if(y == 2 && x == (int)(size * 0.5) + length(exitAndSave)/2 - length(touchEXIT)/2) curString = touchEXIT;
				else if(y == 3 && x == (int)(size * 0.5)) curString = exitAndSave;

				menu += curString;
				x+=length(curString);
			}
			menu = menu + Cara.CARA_BARRE_VERTICALE + "\n";
		}

		return menu;
	}

	// retourne un titre centré par rapport a la taille donnée
	String menuCreateTitle(int size, String title){
		String curString, txt = "";
		int x;
		for(int y = 0; y < 2; y++){
			txt+=Cara.CARA_BARRE_VERTICALE;
			x = 0;
			while(x < size){
				curString = " ";
				if(y == 0 && x == size/2 - length(title)/2){
					curString = title;
				}
				txt+=curString;
				x+=length(curString);
			}
			txt+=Cara.CARA_BARRE_VERTICALE + "\n";
		}
		return txt;
	}

	// retourne une barre qui peut avoir differente forme selon l'indice donné en parametre
	String menuCreateBarre(int size, int index){
		char b_debut = Cara.CARA_ESPACE, b_fin = Cara.CARA_ESPACE, b_mid = Cara.CARA_BARRE_HORIZONTALE;
		String barre = "";
		
		if(index == 0 || index == 6){
			b_debut = Cara.CARA_BARRE_COTE_HG;
			b_fin = Cara.CARA_BARRE_COTE_HD;
		}
		else if(index == 1 || index == 2 || index == 7){
			b_debut = Cara.CARA_BARRE_COTE_MG;
			b_fin = Cara.CARA_BARRE_COTE_MD;
		}
		else if(index == 3 || index == 8){
			b_debut = Cara.CARA_BARRE_COTE_BG;
			b_fin = Cara.CARA_BARRE_COTE_BD;
		}
		
		for(int y = 0; y < size;y++){
			b_mid = Cara.CARA_BARRE_HORIZONTALE;

			if(y == (int)(size * ((double)1/(double)3)) || y == (int)(size * ((double)2/(double)3))){
				if(index == 6 || index == 7)
					b_mid = Cara.CARA_BARRE_COTE_HM;
				else if(index == 8)
					b_mid = Cara.CARA_BARRE_COTE_BM;
			}
			
			barre = barre + b_mid;
		}
		return b_debut + barre + b_fin + "\n";
	}

	// ajoute de l'experience (XP) au joueur
	void addExperience(Player player, int exp){
		int maxExperience = getMaxExperience(player.level);
		player.experience+=exp;
		while(player.experience >= maxExperience){
			maxExperience = getMaxExperience(player.level);
			player.experience-= maxExperience;
			player.level++;
		}
	}

	// ajoute un item a l'inventaire du joueur, sinon augmente la quantité
	void addItem(Item[] inventaire, Item item){
		if(possedeItem(inventaire, item.type)){
			for(int i = 0; i < length(inventaire); i++){
				if(inventaire[i].type == item.type){
					inventaire[i].quantity += item.quantity;
					return;
				}
			}
		}else{
			for(int i = 0; i < length(inventaire); i++){
				if(inventaire[i].type == ItemType.Vide){
					inventaire[i] = item;
					return;
				}
			}
		}
	}

	// supprime un item (ou une quantité) dans l'inventaire du joueur
	void removeItem(Item[] inventaire, ItemType type, int quantity){
		int i = 0;
		while(i < length(inventaire)){
			if(inventaire[i].type == type){
				inventaire[i].quantity -= quantity;
				if (inventaire[i].quantity <= 0){
					inventaire[i] = newItem(ItemType.Vide, 0);
				}
				i = length(inventaire);
			}else
				i++;
		}
	}

	// verifie si le joueur a assez de ressources pour avancé dans l'histoire du jeu
	boolean checkItemAvance(Item[] inventaire){
		boolean result = false;
		if(inventaire[0] != null && inventaire[1] != null){
			result =
			getQuantityItemInInventory(inventaire, ItemType.Bois) >= boisAPosseder &&
			getQuantityItemInInventory(inventaire, ItemType.Corde) >= cordeAPosseder;
		}
		return result;
	}

	// utilise les ressources si il peut avancé dans l'histoire du jeu
	void useItemToAvance(Item[] inventaire){
		removeItem(inventaire, ItemType.Bois, boisAPosseder);
		removeItem(inventaire, ItemType.Corde, cordeAPosseder);
	}

	// retourne la quantity d'un item que le joueur possede
	int getQuantityItemInInventory(Item[] inventaire, ItemType type){
		int i = 0, quantity = 0;
		while(i < length(inventaire)){
			if(inventaire[i].type == type){
				quantity = inventaire[i].quantity;
				i = length(inventaire);
			}else
				i++;
		}
		return quantity;
	}

	// verifie si le joueur possede cet item
	boolean possedeItem(Item[] inventaire, ItemType type){
		boolean result = false;
		int i = 0;
		while(i < length(inventaire)){
			if(inventaire[i].type == type){
				result = true;
				i = length(inventaire);
			}else
				i++;
		}
		return result;
	}

	// fonction qui retire de la vie a un joueur
	void takeDamage(Player player, int amount){
		if(player.health - amount > 0)
			player.health -= amount;
		else
			player.health = 0;
	}

	// verifie si le joueur est mort ou non
	boolean playerDead(Player player){
		return player.health <= 0;
	}

	// retourne le nombre maximal d'experience a obtenir par rapport au level du joueur
	int getMaxExperience(int level){
		return 50 + 25 * (level - 1);
	}

	// retourne le nombre maximal de vie qu'un joueur peut avoir
	int getMaxHealth(){
		return 100;
	}
	
	// verifie si le caractere peut etre decouvert dans le mot, si oui, il le decouvre
	boolean decouvreLettre(Lettre[] mots, char c){
		boolean resultat = false;
		for(int i = 0; i < length(mots); i++){
			if(equalsCharacter(mots[i].caractere, c)){
				mots[i].visible = true;
				resultat = true;
			}
		}
		return resultat;
	}

	// verifie si deux caracteres sont égaux (on enleve les majuscules et les accents)
	boolean equalsCharacter(char c1, char c2){
		c1 = removeMaj(c1);
		c2 = removeMaj(c2);

		return removeAccents(c1) == removeAccents(c2);
	}

	// verifie si le caractere donné en parametre est en majuscule ou non
	boolean estMajuscule(char c){
		return c >= 'A' && c <= 'Z';
	}

	// retourne le caractere en minuscule avec les accents
	char removeMaj(char c){
		if (estMajuscule(c)) c += DIFF_CARA;

		if(c == 'É') c = 'é';
		else if(c == 'È') c = 'è';
		else if(c == 'Ë') c = 'ë';
		else if(c == 'Ê') c = 'ê';
		else if(c == 'Ï') c = 'ï';
		else if(c == 'Î') c = 'î';
		else if(c == 'Ö') c = 'ö';
		else if(c == 'Ô') c = 'ô';
		else if(c == 'À') c = 'à';
		else if(c == 'Ù') c = 'ù';
		else if(c == 'Ç') c = 'ç';

		return c;
	}

	// retourne le caractere sans les accents
	char removeAccents(char c){
		if(c == 'é' || c == 'è' || c == 'ë' || c == 'ê') c = 'e';
		else if(c == 'ï' || c == 'î') c = 'i';
		else if(c == 'ö' || c == 'ô') c = 'o';
		else if(c == 'à') c = 'a';
		else if(c == 'ù') c = 'u';
		else if(c == 'ç') c = 'c';

		return c;
	}

	// verifie si toutes les lettres du mots a été decouverts ou non
	boolean allLettersFind(Lettre[] mots){
		boolean valide = true;
		int i = 0;
		while(i < length(mots)){
			if (!mots[i].visible && mots[i].caractere != ' '){
				valide = false;
				i = length(mots);
			}else
				i++;
		}
		return valide;
	}

	// fonction qui lance le combat contre l'ennemi, et demande a l'uilisateur de traduire un mot sous forme de pendu
	void startAttack(Word[] words, Enemy enemy){
		boolean englishOrFrench, combatGagne, gagne = false;
		int currentIndexWord, nbEssaie = 10 - player.level, nbValide = 1, nbAValide = player.level + 1;
		String saisieWord, wantedWord, currentWord, demande = "";
		for(int i = 0; i < 2 + size_ECRAN/2 - length(demande);i++) demande+=" ";
		demande += "Entrez un mot : ";

		Lettre[] mots;

		do{
			combatGagne = false;
			englishOrFrench = random() < 0.5;
			currentIndexWord = getRandomIndexWord(words);

			if(englishOrFrench){
				currentWord = words[currentIndexWord].motEn;
				wantedWord = words[currentIndexWord].motFr;
			}else{
				currentWord = words[currentIndexWord].motFr;
				wantedWord = words[currentIndexWord].motEn;
			}

			nbEssaie+=length(wantedWord)/2;
			mots = new Lettre[length(wantedWord)];
			initMots(mots, wantedWord);

			do{
				clearScreen();
				println(afficherCombat(enemy.indexArt, currentWord, mots, nbEssaie, nbValide, nbAValide));
				saisieWord = readStringNonVide(demande);
				for(int i = 0; i < length(saisieWord); i++){
					if (!decouvreLettre(mots, charAt(saisieWord, i))){
						takeDamage(player, 3);
						nbEssaie--;
					}
				}
				if(allLettersFind(mots)){
					combatGagne = true;
					nbValide++;
				}
			}while(!combatGagne && nbEssaie > 0);
			gagne = nbValide == nbAValide + 1;
		}while(!gagne && nbEssaie > 0 && nbValide <= nbAValide);
		
		if (gagne){
			addExperience(player, 25);
			addItem(player.inventaire, enemy.itemToDrop);
			enemy.dead = true;
		}
	}

	// renvoie un nombre aléatoire sur toute la taille de tous les mots
	int getRandomIndexWord(Word[] words){
		return (int)(length(words) * random());
	}

	// retourne une chaine de caractere contenant l'ascii art demandé via l'indice demandé en parametre
	String getArt(int index){
		String directory = arts_Directory + "art" + index + ".txt";
		File f = newFile(directory);
		String currentLine, txt = "";
		int maxSize = getMaxSizeLine(directory);

		while(ready(f)){
			currentLine = readLine(f);

			txt += Cara.CARA_BARRE_VERTICALE + "";

			for(int i = 0; i < (size_ECRAN - maxSize)/2; i++)
				txt+=" ";

			txt += currentLine;

			for(int i = 0; i < size_ECRAN - (length(currentLine) + (size_ECRAN - maxSize)/2); i++)
				txt+=" ";

			txt+=Cara.CARA_BARRE_VERTICALE+"\n";
		}
		return txt;
	}

	// retourne la taille maximale qu'une ligne peut avoir dans un fichier
	int getMaxSizeLine(String directory){
		File f = newFile(directory);
		int currentSize, maxSize = 0;

		while(ready(f)){
			currentSize = length(readLine(f));
			if(maxSize < currentSize) maxSize = currentSize;
		}

		return maxSize;
	}
	
	// verifie si l'ascii art demandé via l'indice existe ou non
	boolean checkArt(int index){
		boolean valide = false;
		String[] files = getAllFilesFromDirectory(arts_Directory);
		int i = 0;
		while(i < length(files)){
			if(equals(files[i], "art" + index + ".txt")){
				valide = true;
				i = length(files);
			}else
				i++;
		}
		return valide;
	}
	
	// retourne un interface contenant les statistiques du joueur, l'ascii art de l'ennemi, et la traduction a effectuer
	String afficherCombat(int indexArt, String currentWord, Lettre[] lettres, int nbEssaie, int nbValide, int nbAValide){
		String curString, txt = "";
		int x, sizeY = 9;

		txt += menuCreateBarre(size_ECRAN, 0);
		txt += afficherStats(size_ECRAN, player);
		txt += menuCreateBarre(size_ECRAN, 1);
		if(checkArt(indexArt)){
			txt += getArt(indexArt);
			txt += menuCreateBarre(size_ECRAN, 1);
		}
		txt += menuCreateTitle(size_ECRAN, "Attaque");
		
		for(int y = 0; y < sizeY; y++){
			txt+=""+Cara.CARA_BARRE_VERTICALE;
			x = 0;
			while(x < size_ECRAN){
				curString = " ";
				if(y == 2 && x == size_ECRAN / 2 - length(currentWord) / 2){
					curString = currentWord;
				}
				if(y == 4 && x == size_ECRAN / 2 - (length(lettres) - 1)){
					curString = "";
					for(int i = 0; i < length(lettres); i++)
						if (lettres[i].visible || lettres[i].caractere == ' ') curString+=lettres[i].caractere + " ";
						else curString+="_ ";
				}
				if(y == sizeY - 2){
					if(x == 2){
						curString = "Essaie restant : " + nbEssaie;
					}
					if(x == size_ECRAN - 8){
						curString = nbValide + "/" + nbAValide;
					}
				}
				txt+=curString;
				x+=length(curString);
			}
			txt+=Cara.CARA_BARRE_VERTICALE + "\n";
		}

		return txt + menuCreateBarre(size_ECRAN, 3);
	}

	// fonction qui passe beaucoup de lignes afin d'avoir un terminal "propre"
	public void clearScreen() {
		System.out.print("\033[H\033[2J");
		System.out.flush();
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////// -- Fonction test -- ////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////

	void test_allEnemyDead(){
		Enemy[] enemies = new Enemy[]{newEnemy(0, 0, 0, 0, newItem(ItemType.Vide, 0))};
		assertFalse(allEnemyDead(enemies));
		enemies[0].dead = true;
		assertTrue(allEnemyDead(enemies));
	}

	void test_estChiffre(){
		assertFalse(estChiffre('A'));
		assertTrue(estChiffre('2'));
		assertTrue(estChiffre('5'));
	}

	void test_estChiffreValide(){
		assertFalse(estChiffreValide(0));
		assertTrue(estChiffreValide(1));
		assertTrue(estChiffreValide(3));
		assertFalse(estChiffreValide(4));
	}

	void test_getSeparator(){
		assertEquals(0, getSeparator(";", ';'));
		assertEquals(-1, getSeparator(";", '-'));
		assertEquals(4, getSeparator("test;test", ';'));
		assertEquals(-1, getSeparator("test;test", '-'));
	}

	void test_getTypeByName(){
		assertEquals(ItemType.Vide, getTypeByName("Vide"));
		assertEquals(ItemType.Bois, getTypeByName("Bois"));
		assertEquals(ItemType.Corde, getTypeByName("Corde"));
		assertNotEquals(ItemType.Vide, getTypeByName("Bois"));
	}

	void test_getEnemyInPos(){
		Enemy ennemi_1 = newEnemy(0, 0, 5, 10, newItem(ItemType.Vide, 0));
		Enemy ennemi_2 = newEnemy(0, 1, 10, 5, newItem(ItemType.Vide, 0));
		Enemy[] enemies = new Enemy[]{ennemi_1, ennemi_2};
		assertEquals(ennemi_1, getEnemyInPos(enemies, 0, 5, 10));
		assertEquals(ennemi_2, getEnemyInPos(enemies, 1, 10, 5));
		assertNotEquals(ennemi_1, getEnemyInPos(enemies, 0, 10, 5));
	}

	void test_positionValide(){
		char[][] map = new char[][]{{'E','E','E'},{'H','H','H'},{'E','E','E'}};
		assertTrue(positionValide(map, 1, 1));
		assertFalse(positionValide(map, -1, 1));
		assertFalse(positionValide(map, 0, 0));
	}

	void test_initPlayerPosition(){
		Player joueur = newPlayer("joueurTest", 0, 0, 0, 0, 0, 0, 0);
		initPlayerPosition(joueur, 5, 10);
		assertEquals(5, joueur.posY);
		assertEquals(10, joueur.posX);
	}

	void test_checkPos(){
		Enemy ennemi_1 = newEnemy(0, 0, 5, 10, newItem(ItemType.Vide, 0));
		assertTrue(checkPos(ennemi_1, 0, 5, 10));
		assertFalse(checkPos(ennemi_1, 0, 10, 5));
	}

	void test_addZeroToNumber(){
		assertEquals("000", addZeroToNumber(0, 3));
		assertEquals("0010", addZeroToNumber(10, 4));
		assertEquals("800", addZeroToNumber(800, 3));
		assertEquals("999", addZeroToNumber(10000000, 3));
	}

	void test_getNamePlayerWithSize(){
		assertEquals("SAE_TEST", getNamePlayerWithSize("SAE_TEST", 50));
		assertEquals("bidule", getNamePlayerWithSize("bidule", 6));
		assertEquals("bi...", getNamePlayerWithSize("bidule", 5));
		assertEquals("truc", getNamePlayerWithSize("truc", 4));
		assertEquals("...", getNamePlayerWithSize("truc", 3));
	}

	void test_tabContainString(){
		String[] tab = new String[]{"un","deux","trois","quatre","cinq"};
		assertTrue(tabContainString(tab, "deux"));
		assertFalse(tabContainString(tab, "six"));
	}

	void test_addExperience(){
		Player joueur = newPlayer("joueurTest", 0, 0, 0, 0, 0, 0, 0);
		assertEquals(0, joueur.experience);
		addExperience(joueur, 10);
		assertEquals(10, joueur.experience);
	}

	void test_addItem(){
		Item[] inventaire = new Item[2];
		initInventaire(inventaire);
		assertEquals(ItemType.Vide, inventaire[0].type);
		assertEquals(ItemType.Vide, inventaire[1].type);

		addItem(inventaire, newItem(ItemType.Bois, 1));
		assertEquals(ItemType.Bois, inventaire[0].type);
		addItem(inventaire, newItem(ItemType.Corde, 1));
		assertEquals(ItemType.Corde, inventaire[1].type);
	}

	void test_removeItem(){
		Item[] inventaire = new Item[2];
		initInventaire(inventaire);
		addItem(inventaire, newItem(ItemType.Bois, 1));
		assertEquals(ItemType.Bois, inventaire[0].type);
		removeItem(inventaire, ItemType.Bois, 1);
		assertEquals(ItemType.Vide, inventaire[0].type);
	}

	void test_checkItemAvance(){
		Item[] inventaire = new Item[2];
		initInventaire(inventaire);

		assertFalse(checkItemAvance(inventaire));

		addItem(inventaire, newItem(ItemType.Bois, boisAPosseder));
		addItem(inventaire, newItem(ItemType.Corde, cordeAPosseder));

		assertTrue(checkItemAvance(inventaire));
	}

	void test_useItemToAvance(){
		Item[] inventaire = new Item[2];
		initInventaire(inventaire);
		addItem(inventaire, newItem(ItemType.Bois, boisAPosseder));
		addItem(inventaire, newItem(ItemType.Corde, cordeAPosseder));

		useItemToAvance(inventaire);

		assertFalse(checkItemAvance(inventaire));
	}

	void test_getQuantityItemInInventory(){
		Item[] inventaire = new Item[2];
		initInventaire(inventaire);
		addItem(inventaire, newItem(ItemType.Bois, boisAPosseder));
		addItem(inventaire, newItem(ItemType.Corde, cordeAPosseder));

		assertEquals(cordeAPosseder, getQuantityItemInInventory(inventaire, ItemType.Corde));
	}

	void test_possedeItem(){
		Item[] inventaire = new Item[2];
		initInventaire(inventaire);
		addItem(inventaire, newItem(ItemType.Bois, 1));

		assertTrue(possedeItem(inventaire, ItemType.Bois));
		assertFalse(possedeItem(inventaire, ItemType.Corde));
	}

	void test_takeDamage(){
		Player joueur = newPlayer("joueurTest", 0, 0, 0, 0, 100, 0, 0);
		assertEquals(100, joueur.health);
		takeDamage(joueur, 20);
		assertEquals(80, joueur.health);
		takeDamage(joueur, 20);
		assertEquals(60, joueur.health);
	}

	void test_playerDead(){
		Player joueur = newPlayer("joueurTest", 0, 0, 0, 0, 100, 0, 0);
		assertFalse(playerDead(joueur));
		takeDamage(joueur, 100);
		assertTrue(playerDead(joueur));
	}

	void test_getMaxExperience(){
		int level = 1;
		assertEquals(50, getMaxExperience(level));
		level++;
		assertEquals(75, getMaxExperience(level));
		level++;
		assertEquals(100, getMaxExperience(level));
	}

	void test_getMaxHealth(){
		assertEquals(100, getMaxHealth());
	}

	void test_equalsCharacter(){
		assertTrue(equalsCharacter('A', 'a'));
		assertTrue(equalsCharacter('a', 'A'));
		assertTrue(equalsCharacter('É', 'é'));
		assertTrue(equalsCharacter('é', 'e'));
		assertTrue(equalsCharacter('î', 'i'));
		assertTrue(equalsCharacter('ô', 'O'));
	}

	void test_estMajuscule(){
		assertTrue(estMajuscule('A'));
		assertTrue(estMajuscule('E'));
		assertTrue(estMajuscule('Z'));
		assertFalse(estMajuscule('a'));
		assertFalse(estMajuscule('e'));
		assertFalse(estMajuscule('z'));
	}

	void test_removeMaj(){
		assertEquals('e', removeMaj('E'));
		assertEquals('a', removeMaj('A'));
		assertEquals('é', removeMaj('É'));
		assertEquals('é', removeMaj('é'));

		assertNotEquals('E', removeMaj('E'));
		assertNotEquals('é', removeMaj('È'));
	}

	void test_removeAccents(){
		assertEquals('e', removeAccents('é'));
		assertEquals('a', removeAccents('à'));
		assertEquals('i', removeAccents('î'));
	}

	void test_allLettersFind(){
		String motString = "Bidule";
		Lettre[] mot = new Lettre[length(motString)];
		initMots(mot, motString);
		assertFalse(allLettersFind(mot));
		for(int i = 0; i < length(mot); i++){
			if(!mot[i].visible){
				decouvreLettre(mot, mot[i].caractere);
			}
		}
		assertTrue(allLettersFind(mot));
	}
}