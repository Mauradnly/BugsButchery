package com.bugsButchery.demo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Service
public class BugsButcheryService {

	@Autowired
	TerritoryRepository myTerritoryRepository;
	
	@Autowired
	FamilyRepository myFamilyRepository;
	
	
	@Autowired
	Game myGame;



	//Trouver ou appeler ces fonctions !!
	public void createAllTerritories() {
    for(Territory entry : myTerritoryRepository.findAll()) {
    	myGame.getAllTerritories().add(entry);
    }
	}

	
	public void createAllFamilies() {
	    for(Family entry : myFamilyRepository.findAll()) {
	    	myGame.getAllFamilies().add(entry);
	    }
		}
	//New Game
	/**
	 * check if all territory are assigned to a player
	 * @return
	 */
	public boolean checkAllTerritoryPicked() {
		List<Territory> allTerritory = new ArrayList<Territory>();
		allTerritory = myTerritoryRepository.findAll();
		for (Territory entry : allTerritory) {
			if (entry.getTerritoryOwner() == null) {
				return false;
			}
		}
		return true;
	}

	//New Round


	/** 
	 * Check if a player owns an entire family
	 * @param player
	 * @return void
	 * @author Eloise
	 */
	public void upDatePlayerTerritoryFamilyList(Player player) {
		
		for (Territory t : player.getPlayerTerritoryList()) {
			ArrayList<Territory> allTerritoryInAFamily = myTerritoryRepository.findAllByTerritoryFamily(t.getTerritoryFamily());
			if(player.getPlayerTerritoryList().containsAll(allTerritoryInAFamily)){
				String newFamily = null;
				player.getPlayerTerritoryFamilyList().add(t.getTerritoryFamily());
				for(Family f : myGame.getAllFamilies()) {
					if(f.getFamilyId() == t.getTerritoryFamily()) { 
						newFamily = f.getFamilyName();
				} 
					}
				myGame.setMessage( player + " a acquis tous les territoires de la famille : "+ newFamily + " !")	;

			}
		}
	}

//	/**
//	 * Calculate the Refill for a new round
//	 * @param player
//	 * @return int Refill
//	 * @author Eloise
//	 */
	public void refillAvailableAnts(Player player) {

		upDatePlayerTerritoryFamilyList(player);

		int refillByTerritory; 
		if((player.getPlayerTerritoryList().size()/3) <= 3) {
		
			refillByTerritory = 3;
		} else {
			refillByTerritory = player.getPlayerTerritoryList().size()/3;
		}
		int refillByFamily = 0;
		for (int f : player.getPlayerTerritoryFamilyList()) {
			refillByFamily =+ myFamilyRepository.findById(f).get().getFamilyValue();
		}
		int refillAvailableAnts = refillByTerritory + refillByFamily;
		player.setPlayerAvailableAnts(refillAvailableAnts);
		
		myGame.setMessage(player +" a reçu " + refillAvailableAnts + " nouvelles fourmis !");

	}

	//Phase 2 attack /optional
	/**
	 * check country pawn number if pawn < 1 can't attack.
	 * @param id
	 * @return
	 */
	public boolean antNumber(Territory territory) {
		if (territory.getTerritoryAntsNb() > 1) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * check if the player have at least 1 pawn left on the country
	 * @param country
	 * @param nbrDiceAttack
	 * @return
	 */
	public boolean oneAntBehind(Territory territory, int nbrDiceAttack) {
		if (territory.getTerritoryAntsNb() > nbrDiceAttack) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * check if country as path to target country
	 * @param idCurrent
	 * @param idTarget
	 * @return
	 */
	public boolean pathExist(Territory attacker, Territory target) {
		if(attacker.getTerritoryFrontiers().contains(target)) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Regroup all 3 checks (antNumber, oneAntBehind and pathExist)
	 * @param attacker
	 * @param target
	 * @param nbrDiceAttack
	 * @return
	 */
	public boolean requestAttack(Territory attacker, Territory target, int nbrDiceAttack) {
		if (antNumber(attacker) && pathExist(attacker, target) && oneAntBehind(attacker, nbrDiceAttack)) {
			myGame.setMessage(attacker.getTerritoryOwner().getPlayerName() + " attaque " + target.getTerritoryOwner().getPlayerName() + " sur "+ target + " depuis " + target +" avec " + nbrDiceAttack + " fourmis !");
			return true;
		}
		else {
			myGame.setMessage(attacker.getTerritoryOwner().getPlayerName() + "ne peut pas attaquer ! Rappel : le territoire attaqué doit être limitrophe au territoire depuis lequel vous attaquez. Au moins une fourmi sur le territoire doit rester en dehors de l'attaque afin de conserver le territoire. Arrêtez de déranger le serveur avec vos bêtises");
			return false;
		}
	}
	
	/** check if the defender has enough pawns to fightBack
	 * @param territory
	 * @param nbrDiceDefense
	 * @return 
	 */
	public boolean requestDefense(Territory defender, int nbrDiceDefense) {
		if (defender.getTerritoryAntsNb() >= nbrDiceDefense && nbrDiceDefense <= 2) {
			myGame.setMessage(defender.getTerritoryOwner().getPlayerName() + "réplique avec " + nbrDiceDefense + " fourmis !");
			return true;
		}
		else {
			myGame.setMessage(defender.getTerritoryOwner().getPlayerName() + "ne peut pas répliquer avec ce nombre de fourmis ! Elles doivent être inférieures à 2 et supérieures au nombre total de fourmis sur le territoire. Arrêtez de déranger le serveur avec vos bêtises");
			return false;
		}
	}
	

	/**
	 * Check if player has conquiered the territory
	 * @param country
	 * @param player
	 * @return
	 */
	public boolean checkConquest(Territory territory) {
		if (territory.getTerritoryAntsNb() > 0) {
			return false;
		}
		else {
			return true;
		}
	}

	/**
	 * Moving through the all fight
	 * @param current
	 * @param attacker
	 * @param nbrDiceAttack
	 * @param defender
	 * @param target
	 * @param nbrDiceDefender
	 */
	public void diceFight(Player current, Territory attacker, int nbrDiceAttack, Player defender, Territory target, int nbrDiceDefender){
		if(requestAttack(attacker, target, nbrDiceAttack) && requestDefense(target, nbrDiceDefender)) {
			ArrayList<Integer> resultCurrent = new ArrayList<Integer>();
			ArrayList<Integer> resultTarget = new ArrayList<Integer>();
			for (int i = 0; i < nbrDiceAttack; i++) {
				resultCurrent.add(current.rollDice());
			}
			for (int i = 0; i < nbrDiceDefender; i++) {
				resultTarget.add(defender.rollDice());
			}

			Collections.sort(resultCurrent, Collections.reverseOrder());
			Collections.sort(resultTarget, Collections.reverseOrder());
			

			if (resultCurrent.size() < 2 || resultTarget.size() < 2) {
				if (resultCurrent.get(0) > resultTarget.get(0)) {
					System.out.println("target -1");
					target.setTerritoryAntsNb(target.getTerritoryAntsNb()-1);
					myGame.setMessage(current + " a lancé son dés ! Il a fait :" + resultCurrent + ". "+ defender + "a lancé son dés et il a fait : " + resultTarget+ " ." + current + " a gagné le combat ! " + defender + " perd donc une fourmi..." ); 
					
				}
				else {
					System.out.println("attacker -1");
					attacker.setTerritoryAntsNb(attacker.getTerritoryAntsNb()-1);
					myGame.setMessage(current + " a lancé son dés ! Il a fait :" + resultCurrent + ". "+ defender + "a lancé son dés et il a fait : " + resultTarget+ " ." + defender + " a gagné le combat ! " + current + " perd donc une fourmi..." );
				}
			}
			else if (resultCurrent.get(0) > resultTarget.get(0) && resultCurrent.get(1) > resultTarget.get(1)) {
				System.out.println("target -2");
				target.setTerritoryAntsNb(target.getTerritoryAntsNb()-2);
				myGame.setMessage(current + " a lancé ses dés ! Il a fait :" + resultCurrent + ". "+ defender + "a lancé ses dés et il a fait : " + resultTarget+ " ." + current + " a gagné les deux tours ! " + defender + " perd donc deux fourmis..." );
			}
			else if (resultCurrent.get(0) > resultTarget.get(0) && resultCurrent.get(1) <= resultTarget.get(1)) {
				System.out.println("attacker -1 target -1");
				attacker.setTerritoryAntsNb(attacker.getTerritoryAntsNb()-1);
				target.setTerritoryAntsNb(target.getTerritoryAntsNb()-1);
				myGame.setMessage(current + " a lancé ses dés ! Il a fait :" + resultCurrent + ". "+ defender + "a lancé ses dés et il a fait : " + resultTarget+ " ." + current + " a perdu un des deux tours ! Il perd donc une fourmi... " + defender + " a perdu un des deux tours ! Il perd donc deux fourmis..." );
			}
			else if (resultCurrent.get(0) <= resultTarget.get(0) && resultCurrent.get(1) > resultTarget.get(1)) {
				System.out.println("attacker -1 target -1");
				attacker.setTerritoryAntsNb(attacker.getTerritoryAntsNb()-1);
				target.setTerritoryAntsNb(target.getTerritoryAntsNb()-1);
				myGame.setMessage(current + " a lancé ses dés ! Il a fait :" + resultCurrent + ". "+ defender + "a lancé ses dés et il a fait : " + resultTarget+ " ." + current + " a perdu un des deux tours ! Il perd donc une fourmi... " + defender + " a perdu un des deux tours ! Il perd donc deux fourmis..." );
			}
			else {
				System.out.println("attacker -2");
				attacker.setTerritoryAntsNb(attacker.getTerritoryAntsNb()-2);
				myGame.setMessage(current + " a lancé ses dés ! Il a fait :" + resultCurrent + ". "+ defender + "a lancé ses dés et il a fait : " + resultTarget+ " ." + current + " a perdu les deux tours ! Il perd donc deux fourmis... " );
			}
			if(checkConquest(target)) {
				//move()
				killAntHill(defender, target);
				defender.getPlayerTerritoryList().remove(target);
				target.setTerritoryOwner(current);
				current.getPlayerTerritoryList().add(target);
				myGame.setMessage(current + " a lancé ses dés ! Il a fait :" + resultCurrent + ". "+ defender + "a lancé ses dés et il a fait : " + resultTarget+ " ." + defender + " a perdu un des deux tours ! Il perd" + nbrDiceDefender + "de ses fourmis...  et perd donc son territoire..."  );
			}
		}
		else {
			System.out.println("cant fight");
			myGame.setMessage("Jeu impossible ! arrêtez d'embêter le serveur avec vos bêtises !" );
		}
	}

	
	public void killAntHill(Player player, Territory territory) {
		if(territory.isAnthill()) {
			for (Territory entry :player.getPlayerTerritoryList()) {
				myGame.getPlayersAlive().remove(player);
				entry.setTerritoryOwner(null);
				myGame.getUnownedTerritories().add(entry);
			}
			myGame.setMessage("FOURMILIERE TOUCHEE !! " + player + " a gagné le tour et "+ territory.getTerritoryOwner().getPlayerName() + "perd son territoire qui était en fait... sa fourmilière ! Bye Bye " + territory.getTerritoryOwner().getPlayerName() + " !"  );
		}
	}
	

	/**
	 * Moving ants after a conquest between the two territories
	 * @param attacker
	 * @param target
	 * @param pawnNbr
	 */
//		public void moveAfterConquest(Territory attacker, Territory target, int antNbr) {
//			(attacker.getTerritoryAntsNb() - antNbr);
//			(target.getTerritoryAntsNb() + antNbr);
//			//NOT FINISH 
//		}


	//---- MOVE ----//

	public boolean moveAvailable(Player player, Territory territoryStart, Territory territoryArrival, int antNbr ) {	

		if (antNbr>=territoryStart.getTerritoryAntsNb()) {
			myGame.setMessage(territoryStart.getTerritoryOwner().getPlayerName() + " ne peut pas déplacer " + antNbr + " depuis "+ territoryStart + " sur " + territoryArrival + " : il n'a pas assez de fourmis ! " );
			return false;
			}
		
		// VALEURS INITIALES
		myGame.getPotentialsTerritories().clear();
		myGame.getPotentialsTerritories().addAll(player.getPlayerTerritoryList());
		myGame.getPotentialsTerritories().addAll(myGame.getUnownedTerritories());
		//	crossedTerritories = new ArrayList<Territory>();          
		myGame.setPathExist(0);
		boolean thereIsAPath=false;

		if(!myGame.getPotentialsTerritories().contains(territoryArrival)){
			myGame.setMessage(territoryStart.getTerritoryOwner().getPlayerName() + " ne peut pas déplacer " + antNbr + " depuis "+ territoryStart + " sur " + territoryArrival + " : il n'existe aucun chemin d'accès! " );
			return false;
		}

		
		moveOneStep(territoryStart, territoryArrival);

		if (myGame.getPathExist()==1) {
			System.out.println("Depart"+territoryStart.getTerritoryName());
			System.out.println("Arrivée"+territoryArrival.getTerritoryName());
			territoryStart.setTerritoryAntsNb(territoryStart.getTerritoryAntsNb()-antNbr);
			System.out.println("antnb:  "+antNbr);
			System.out.println(territoryArrival.getTerritoryAntsNb());
			territoryArrival.setTerritoryAntsNb(territoryArrival.getTerritoryAntsNb()+antNbr);
			thereIsAPath=true;
		}
		myGame.setMessage(territoryStart.getTerritoryOwner().getPlayerName() + " a déplacé " + antNbr + " depuis "+ territoryStart + " sur " + territoryArrival );
		return thereIsAPath;
	}


	public boolean moveOneStep(Territory territory1, Territory territory2) {
		
		myGame.getPotentialsTerritories().remove(territory1);
		if(myGame.getPotentialsTerritories().size()==0) {
			return false;
		}//if 0 territory except territory1 (||& territories already crossed) = false
		
		List<Territory> TerritoryFrontiersMine =territory1.getTerritoryFrontiers(); 
		TerritoryFrontiersMine.retainAll(myGame.getPotentialsTerritories()); // valeurs de territoryFrontierMine se croisent avec les territoires frontaliers (also return true)


		if(TerritoryFrontiersMine.size()==0) {
			return false;
		} // si pas de territoires frontaliers = false


		else { 

			for (Territory thisTerritory : TerritoryFrontiersMine) { // pour les territoires frontaliers

				if (thisTerritory.equals(territory2)) {
					myGame.setPathExist(1);
				} //quand la boucle tombe sur le territoire de destination,  = path exist

				else {
					//				crossedTerritories.add(thisTerritory);
					//				potentialsTerritories.removeAll(crossedTerritories);
					myGame.getPotentialsTerritories().remove(thisTerritory); //supprime de la liste des territoires à traiter
					moveOneStep(thisTerritory, territory2); // continue à chercher le territoire de à partir de la nouvelle position 'thisterritoy'
				}
			}

		}
		return false;
	}


	//---- Change player ----//

	public void changePlayer() {
		myGame.setMessage(myGame.playerTurn.getPlayerName() + " a fini son tour !");
		int roundSize= myGame.getPlayersAlive().size();
		int roundPosition= myGame.getPlayersAlive().indexOf(myGame.getPlayerTurn());
		if(roundSize!=roundPosition+1) {
			myGame.setPlayerTurn(myGame.getPlayersAlive().get(roundPosition+1));
		}  
		else {
			myGame.setPlayerTurn(myGame.getPlayersAlive().get(0));
		}
		myGame.setMessage("C'est maintenant au tour de " + myGame.playerTurn.getPlayerName() + " de jouer ");
	}
	
	
	
	
	public void addPlayer(Player thisPlayer) {
		myGame.getPlayersAlive().add(thisPlayer);
		myGame.setMessage(thisPlayer.getPlayerName() + " a rejoint la partie !");
	}

	public boolean createNewPlayer(Player thisPlayer) {		
		if(myGame.playersAlive.size() < 4) {
			thisPlayer.setPlayerAvailableAnts(15);
			myGame.getPlayersAlive().add(thisPlayer);
			return true;
		} else {
			return false;
		}

	}
	

	/**
	 * placer le nombre de fourmis que l'on veut sur un territoire possédé
	 * @param player
	 * @param territory
	 * @param ants
	 * @return
	 */
	public boolean placeAnts(Player player, Territory territory, int ants) {
		boolean check=false;
		if (player.getPlayerTerritoryList().contains(territory) && ants<= player.getPlayerAvailableAnts()) {
		//si le player possède le territoire (nommé ici territory) qu'on fait passer dans la méthode	
			territory.setTerritoryAntsNb(territory.getTerritoryAntsNb() + ants);
			//le territoire possédé ...
			player.setPlayerAvailableAnts(player.getPlayerAvailableAnts() - ants);
			myGame.setMessage(player.getPlayerName() + " a placé " + ants + " fourmis sur " + territory + ". ");
			check=true;
		}
		

		return check;
		
		
		//return player.getPlayerTerritoryList();
		//retourn la liste des territoires qui on changé dans la methode
	}

	/**
	 * placer tour a tour une fourmi pour définir a qui sont les territoires
	 * @param player
	 * @param territory
	 * @return
	 */
	public ArrayList<Territory> placeFirstAnts(Player player, Territory territory) {
		
		if (territory.getTerritoryOwner() == null) {
		//si le territoire séléctionner est égal a vide
			player.getPlayerTerritoryList().add(territory);
			territory.setTerritoryOwner(player);
			//ajoute territoire a la liste de territoire du player
			player.setPlayerAvailableAnts(player.getPlayerAvailableAnts() - 1);
			//enlever une fourmi au compte total de fourmi du player
			myGame.setMessage(player.getPlayerName() + " a pris possession de " + territory + ". ");
		}
		return player.getPlayerTerritoryList();
		//retourn la liste des territoires qui on changé dans la methode
	}
	
	/**
	 * @param player
	 * @param territory
	 */
	public void addAntsHill(Player player, Territory territory) {
		if (player.getPlayerTerritoryList().contains(territory)) {
			territory.isAnthill();
			//myGame.setMessage("---TOP SECRET---- vous avez désigné " + territory + "comme votre fourmilière. ");
		}
	}

	
	
	
}
