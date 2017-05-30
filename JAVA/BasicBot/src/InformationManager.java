import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import bwapi.Player;
import bwapi.Position;
import bwapi.Race;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.Unitset;
import bwapi.WeaponType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;
import bwta.Region;

/// ���� ��Ȳ���� �� �Ϻθ� ��ü �ڷᱸ�� �� �����鿡 �����ϰ� ������Ʈ�ϴ� class
/// ���� ���� ��Ȳ������ BWAPI::Broodwar �� ��ȸ�Ͽ� �ľ��� �� ������, ���� ���� ��Ȳ������ BWAPI::Broodwar �� ���� ��ȸ�� �Ұ����ϱ� ������ InformationManager���� ���� �����ϵ��� �մϴ�
/// ����, BWAPI::Broodwar �� BWTA ���� ���� ��ȸ�� �� �ִ� ���������� ��ó�� / ���� �����ϴ� ���� ������ �͵� InformationManager���� ���� �����ϵ��� �մϴ�
public class InformationManager {
	private static InformationManager instance = new InformationManager();

	public Player selfPlayer;		///< �Ʊ� Player		
	public Player enemyPlayer;		///< �Ʊ� Player�� ����		
	public Race selfRace;			///< ���� Player		
	public Race enemyRace;			///< ���� Player�� ����  

	/// �ش� Player�� �ֿ� �ǹ����� �ִ� BaseLocation. 
	/// ó������ StartLocation ���� ����. mainBaseLocation �� ��� �ǹ��� �ı��� ��� ������
	/// �ǹ� ���θ� �������� �ľ��ϱ� ������ �������ϰ� �Ǵ��Ҽ��� �ֽ��ϴ� 
	private Map<Player, BaseLocation> mainBaseLocations = new HashMap<Player, BaseLocation>();

	/// �ش� Player�� mainBaseLocation �� ����Ǿ��°� (firstChokePoint, secondChokePoint, firstExpansionLocation �� ������ �ߴ°�)
	private Map<Player, Boolean> mainBaseLocationChanged = new HashMap<Player, Boolean>();

	/// �ش� Player�� �����ϰ� �ִ� Region �� �ִ� BaseLocation
	/// �ǹ� ���θ� �������� �ľ��ϱ� ������ �������ϰ� �Ǵ��Ҽ��� �ֽ��ϴ� 
	private Map<Player, List<BaseLocation>> occupiedBaseLocations = new HashMap<Player, List<BaseLocation>>();

	/// �ش� Player�� �����ϰ� �ִ� Region
	/// �ǹ� ���θ� �������� �ľ��ϱ� ������ �������ϰ� �Ǵ��Ҽ��� �ֽ��ϴ� 
	private Map<Player, Set<Region>> occupiedRegions = new HashMap<Player, Set<Region>>();

	/// �ش� Player�� mainBaseLocation ���� ���� ����� ChokePoint
	private Map<Player, Chokepoint> firstChokePoint = new HashMap<Player, Chokepoint>();
	/// �ش� Player�� mainBaseLocation ���� ���� ����� BaseLocation
	private Map<Player, BaseLocation> firstExpansionLocation = new HashMap<Player, BaseLocation>();
	/// �ش� Player�� mainBaseLocation ���� �ι�°�� ����� (firstChokePoint�� �ƴ�) ChokePoint
	/// ���� �ʿ� ����, secondChokePoint �� �Ϲ� ��İ� �ٸ� ������ �� ���� �ֽ��ϴ�
	private Map<Player, Chokepoint> secondChokePoint = new HashMap<Player, Chokepoint>();

	/// Player - UnitData(�� Unit �� �� Unit�� UnitInfo �� Map ���·� �����ϴ� �ڷᱸ��) �� �����ϴ� �ڷᱸ�� ��ü
	private Map<Player, UnitData> unitData = new HashMap<Player, UnitData>();

	/// static singleton ��ü�� �����մϴ�
	public static InformationManager Instance() {
		return instance;
	}

	public InformationManager() {
		selfPlayer = MyBotModule.Broodwar.self();
		enemyPlayer = MyBotModule.Broodwar.enemy();
		selfRace = selfPlayer.getRace();
		enemyRace = enemyPlayer.getRace();

		unitData.put(selfPlayer, new UnitData());
		unitData.put(enemyPlayer, new UnitData());
		
		occupiedBaseLocations.put(selfPlayer, new ArrayList<BaseLocation>());
		occupiedBaseLocations.put(enemyPlayer, new ArrayList<BaseLocation>());
		occupiedRegions.put(selfPlayer, new HashSet());
		occupiedRegions.put(enemyPlayer, new HashSet());

		mainBaseLocations.put(selfPlayer, BWTA.getStartLocation(MyBotModule.Broodwar.self()));
		mainBaseLocationChanged.put(selfPlayer, new Boolean(true));

		occupiedBaseLocations.get(selfPlayer).add(mainBaseLocations.get(selfPlayer));
		updateOccupiedRegions(BWTA.getRegion(mainBaseLocations.get(selfPlayer).getTilePosition()),
				MyBotModule.Broodwar.self());

		mainBaseLocations.put(enemyPlayer, null);
		mainBaseLocationChanged.put(enemyPlayer, new Boolean(false));
		
		firstChokePoint.put(selfPlayer, null);
		firstChokePoint.put(enemyPlayer, null);
		firstExpansionLocation.put(selfPlayer, null);
		firstExpansionLocation.put(enemyPlayer, null);
		secondChokePoint.put(selfPlayer, null);
		secondChokePoint.put(enemyPlayer, null);

		updateChokePointAndExpansionLocation();
		
	}

	/// Unit �� BaseLocation, ChokePoint � ���� ������ ������Ʈ�մϴ�
	public void update() {
		updateUnitsInfo();
		// occupiedBaseLocation �̳� occupiedRegion �� ���� �ȹٲ�Ƿ� ���� ���ص� �ȴ�
		if (MyBotModule.Broodwar.getFrameCount() % 120 == 0) {
			updateBaseLocationInfo();
		}
	}

	/// ��ü unit �� ������ ������Ʈ �մϴ� (UnitType, lastPosition, HitPoint ��)
	public void updateUnitsInfo() {
		// update our units info
		for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()) {
			updateUnitInfo(unit);
		}
		for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
			updateUnitInfo(unit);
		}

		// remove bad enemy units
		if (unitData.get(enemyPlayer) != null) {
			unitData.get(enemyPlayer).removeBadUnits();
		}
		if (unitData.get(selfPlayer) != null) {
			unitData.get(selfPlayer).removeBadUnits();
		}
	}

	/// �ش� unit �� ������ ������Ʈ �մϴ� (UnitType, lastPosition, HitPoint ��)
	public void updateUnitInfo(Unit unit) {
		try {
			if (!(unit.getPlayer() == selfPlayer || unit.getPlayer() == enemyPlayer)) {
				return;
			}

			if (enemyRace == Race.Unknown && unit.getPlayer() == enemyPlayer) {
				enemyRace = unit.getType().getRace();
			}
			unitData.get(unit.getPlayer()).updateUnitInfo(unit);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/// Unit �� ���� ������ ������Ʈ�մϴ�
	public void onUnitShow(Unit unit) { 
		updateUnitInfo(unit); 
	}
	/// Unit �� ���� ������ ������Ʈ�մϴ�
	public void onUnitHide(Unit unit) { 
		updateUnitInfo(unit); 
	}
	/// Unit �� ���� ������ ������Ʈ�մϴ�
	public void onUnitCreate(Unit unit) { 
		updateUnitInfo(unit); 
	}
	/// Unit �� ���� ������ ������Ʈ�մϴ�
	public void onUnitComplete(Unit unit) { 
		updateUnitInfo(unit); 
	}
	/// Unit �� ���� ������ ������Ʈ�մϴ�
	public void onUnitMorph(Unit unit) { 
		updateUnitInfo(unit); 
	}
	/// Unit �� ���� ������ ������Ʈ�մϴ�
	public void onUnitRenegade(Unit unit) { 
		updateUnitInfo(unit); 
	}
	
	/// Unit �� ���� ������ ������Ʈ�մϴ� 
	/// ������ �ı�/����� ���, �ش� ���� ������ �����մϴ�
	public void onUnitDestroy(Unit unit) {
		if (unit.getType().isNeutral()) {
			return;
		}

		unitData.get(unit.getPlayer()).removeUnit(unit);
	}


	/// �ش� Player (�Ʊ� or ����) �� position ������ ���� ����� unitInfo �� �����մϴ�		 
	public void getNearbyForce(Vector<UnitInfo> unitInfo, Position p, Player player, int radius) {
		Iterator<Integer> it = getUnitData(player).getUnitAndUnitInfoMap().keySet().iterator();

		// for each unit we know about for that player
		// for (final Unit kv :
		// getUnitData(player).getUnits().keySet().iterator()){
		while (it.hasNext()) {
			final UnitInfo ui = getUnitData(player).getUnitAndUnitInfoMap().get(it.next());

			// if it's a combat unit we care about
			// and it's finished!
			if (isCombatUnitType(ui.getType()) && ui.isCompleted()) {
				// determine its attack range
				int range = 0;
				if (ui.getType().groundWeapon() != WeaponType.None) {
					range = ui.getType().groundWeapon().maxRange() + 40;
				}

				// if it can attack into the radius we care about
				if (ui.getLastPosition().getDistance(p) <= (radius + range)) {
					// add it to the vector
					// C++ : unitInfo.push_back(ui);
					unitInfo.add(ui);
				}
			} else if (ui.getType().isDetector() && ui.getLastPosition().getDistance(p) <= (radius + 250)) {
				// add it to the vector
				// C++ : unitInfo.push_back(ui);
				unitInfo.add(ui);
			}
		}
	}

	/// �ش� Player (�Ʊ� or ����) �� �ش� UnitType ���� ���ڸ� �����մϴ� (�Ʒ�/�Ǽ� ���� ���� ���ڱ��� ����)
	public int getNumUnits(UnitType t, Player player) {
		return getUnitData(player).getNumUnits(t.toString());
	}

	/// �ش� Player (�Ʊ� or ����) �� ��� ���� ��� UnitData �� �����մϴ�		 
	public final UnitData getUnitData(Player player) {
		return unitData.get(player);
	}

	public void updateBaseLocationInfo() {
		if (occupiedRegions.get(selfPlayer) != null) {
			occupiedRegions.get(selfPlayer).clear();
		}
		if (occupiedRegions.get(enemyPlayer) != null) {
			occupiedRegions.get(enemyPlayer).clear();
		}
		if (occupiedBaseLocations.get(selfPlayer) != null) {
			occupiedBaseLocations.get(selfPlayer).clear();
		}
		if (occupiedBaseLocations.get(enemyPlayer) != null) {
			occupiedBaseLocations.get(enemyPlayer).clear();
		}

		// enemy �� startLocation�� ���� �𸣴� ���
		if (mainBaseLocations.get(enemyPlayer) == null) {
			// how many start locations have we explored
			int exploredStartLocations = 0;
			boolean enemyStartLocationFound = false;

			// an unexplored base location holder
			BaseLocation unexplored = null;

			for (BaseLocation startLocation : BWTA.getStartLocations()) {
				if (existsPlayerBuildingInRegion(BWTA.getRegion(startLocation.getTilePosition()), enemyPlayer)) {
					if (enemyStartLocationFound == false) {
						enemyStartLocationFound = true;
						mainBaseLocations.put(enemyPlayer, startLocation);
						mainBaseLocationChanged.put(enemyPlayer, new Boolean(true));
					}
				}

				if (MyBotModule.Broodwar.isExplored(startLocation.getTilePosition())) {
					// if it's explored, increment
					exploredStartLocations++;
				} else {
					// otherwise set it as unexplored base
					unexplored = startLocation;
				}
			}

			// if we've explored every start location except one, it's the enemy
			if (!enemyStartLocationFound && exploredStartLocations == ((int) BWTA.getStartLocations().size() - 1)) {
				enemyStartLocationFound = true;
				mainBaseLocations.put(enemyPlayer, unexplored);
				mainBaseLocationChanged.put(enemyPlayer, new Boolean(true));				
				// C++ : _occupiedBaseLocations[_enemy].push_back(unexplored);
				if(occupiedBaseLocations.get(enemyPlayer) == null)
				{
					occupiedBaseLocations.put(enemyPlayer, new ArrayList<BaseLocation>()); 
				}
				occupiedBaseLocations.get(enemyPlayer).add(unexplored);
			}
		}

		// update occupied base location
		// � Base Location ���� �Ʊ� �ǹ�, ���� �ǹ� ��� ȥ�����־ ���ÿ� ���� Player �� Occupy �ϰ�
		// �ִ� ������ ������ �� �ִ�
		for (BaseLocation baseLocation : BWTA.getBaseLocations()) {
			if (hasBuildingAroundBaseLocation(baseLocation, enemyPlayer)) {
				// C++ : _occupiedBaseLocations[_enemy].push_back(baseLocation);
				occupiedBaseLocations.get(enemyPlayer).add(baseLocation);
			}

			if (hasBuildingAroundBaseLocation(baseLocation, selfPlayer)) {
				// C++ : _occupiedBaseLocations[_self].push_back(baseLocation);
				occupiedBaseLocations.get(selfPlayer).add(baseLocation);
			}
		}

		// enemy�� mainBaseLocations�� �߰��� ��, �װ��� �ִ� �ǹ��� ��� �ı��� ���
		// _occupiedBaseLocations �߿��� _mainBaseLocations �� �����Ѵ�
		if (mainBaseLocations.get(enemyPlayer) != null) {
			if (existsPlayerBuildingInRegion(BWTA.getRegion(mainBaseLocations.get(enemyPlayer).getTilePosition()), enemyPlayer) == false) {
				for (BaseLocation loaction : occupiedBaseLocations.get(enemyPlayer)) {
					if (existsPlayerBuildingInRegion(BWTA.getRegion(loaction.getTilePosition()),enemyPlayer)) {
						mainBaseLocations.put(enemyPlayer, loaction);
						mainBaseLocationChanged.put(enemyPlayer, new Boolean(true));				
						break;
					}
				}
			}
		}

		// self�� mainBaseLocations�� ����, �װ��� �ִ� �ǹ��� ��� �ı��� ���
		// _occupiedBaseLocations �߿��� _mainBaseLocations �� �����Ѵ�
		if (mainBaseLocations.get(selfPlayer) != null) {
			if (existsPlayerBuildingInRegion(BWTA.getRegion(mainBaseLocations.get(selfPlayer).getTilePosition()), selfPlayer) == false) {
				for (BaseLocation loaction : occupiedBaseLocations.get(selfPlayer)) {
					if (existsPlayerBuildingInRegion(BWTA.getRegion(loaction.getTilePosition()), selfPlayer)) {
						mainBaseLocations.put(selfPlayer, loaction);
						mainBaseLocationChanged.put(selfPlayer, new Boolean(true));				
						break;
					}
				}
			}
		}

		Iterator<Integer> it = null;
		if (unitData.get(enemyPlayer) != null) {
			it = unitData.get(enemyPlayer).getUnitAndUnitInfoMap().keySet().iterator();

			// for each enemy building unit we know about
			// for (const auto & kv : unitData.get(enemy).getUnits())
			while (it.hasNext()) {
				final UnitInfo ui = unitData.get(enemyPlayer).getUnitAndUnitInfoMap().get(it.next());
				if (ui.getType().isBuilding()) {
					updateOccupiedRegions(BWTA.getRegion(ui.getLastPosition().toTilePosition()),
							MyBotModule.Broodwar.enemy());
				}
			}
		}

		if (unitData.get(selfPlayer) != null) {
			it = unitData.get(selfPlayer).getUnitAndUnitInfoMap().keySet().iterator();

			// for each of our building units
			// for (const auto & kv : _unitData[_self].getUnits())
			while (it.hasNext()) {
				final UnitInfo ui = unitData.get(selfPlayer).getUnitAndUnitInfoMap().get(it.next());
				if (ui.getType().isBuilding()) {
					updateOccupiedRegions(BWTA.getRegion(ui.getLastPosition().toTilePosition()),
							MyBotModule.Broodwar.self());
				}
			}
		}

		updateChokePointAndExpansionLocation();
	}

	public void updateChokePointAndExpansionLocation() {
		if (mainBaseLocationChanged.get(selfPlayer).booleanValue() == true) {
		
			if (mainBaseLocations.get(selfPlayer) != null) {
				BaseLocation sourceBaseLocation = mainBaseLocations.get(selfPlayer);
	
				firstChokePoint.put(selfPlayer, BWTA.getNearestChokepoint(sourceBaseLocation.getTilePosition()));
							
				double tempDistance;
				double closestDistance = 1000000000;
				for (BaseLocation targetBaseLocation : BWTA.getBaseLocations())
				{
					if (targetBaseLocation.getTilePosition().equals(mainBaseLocations.get(selfPlayer).getTilePosition())) continue;
	
					tempDistance = BWTA.getGroundDistance(sourceBaseLocation.getTilePosition(), targetBaseLocation.getTilePosition());
					if (tempDistance < closestDistance && tempDistance > 0) {
						closestDistance = tempDistance;
						firstExpansionLocation.put(selfPlayer, targetBaseLocation);
					}
				}
	
				closestDistance = 1000000000;
				for(Chokepoint chokepoint : BWTA.getChokepoints() ) {
					if ( chokepoint.getCenter().equals(firstChokePoint.get(selfPlayer).getCenter())) continue;
	
					tempDistance = BWTA.getGroundDistance(sourceBaseLocation.getTilePosition(), chokepoint.getCenter().toTilePosition());
					if (tempDistance < closestDistance && tempDistance > 0) {
						closestDistance = tempDistance;
						secondChokePoint.put(selfPlayer, chokepoint);
					}
				}
			}
			mainBaseLocationChanged.put(selfPlayer, new Boolean(false));
		}
		
		if (mainBaseLocationChanged.get(enemyPlayer).booleanValue() == true) {

	
			if (mainBaseLocations.get(enemyPlayer) != null) {
				BaseLocation sourceBaseLocation = mainBaseLocations.get(enemyPlayer);
	
				firstChokePoint.put(enemyPlayer, BWTA.getNearestChokepoint(sourceBaseLocation.getTilePosition()));
				
				double tempDistance;
				double closestDistance = 1000000000;
				for (BaseLocation targetBaseLocation : BWTA.getBaseLocations())
				{
					if (targetBaseLocation.getTilePosition().equals(mainBaseLocations.get(enemyPlayer).getTilePosition())) continue;
	
					tempDistance = BWTA.getGroundDistance(sourceBaseLocation.getTilePosition(), targetBaseLocation.getTilePosition());
					if (tempDistance < closestDistance && tempDistance > 0) {
						closestDistance = tempDistance;
						firstExpansionLocation.put(enemyPlayer, targetBaseLocation);
					}
				}
	
				closestDistance = 1000000000;
				for(Chokepoint chokepoint : BWTA.getChokepoints() ) {
					if ( chokepoint.getCenter().equals(firstChokePoint.get(enemyPlayer).getCenter())) continue;
	
					tempDistance = BWTA.getGroundDistance(sourceBaseLocation.getTilePosition(), chokepoint.getCenter().toTilePosition());
					if (tempDistance < closestDistance && tempDistance > 0) {
						closestDistance = tempDistance;
						secondChokePoint.put(enemyPlayer, chokepoint);
					}
				}
			}
			mainBaseLocationChanged.put(enemyPlayer, new Boolean(false));
		}
	}

	public void updateOccupiedRegions(Region region, Player player) {
		// if the region is valid (flying buildings may be in null regions)
		if (region != null) {
			// add it to the list of occupied regions
			if (occupiedRegions.get(player) == null) {
				occupiedRegions.put(player, new HashSet<Region>());
			}
			occupiedRegions.get(player).add(region);
		}
	}

	/// �ش� BaseLocation �� player�� �ǹ��� �����ϴ��� �����մϴ�
	/// @param baseLocation ��� BaseLocation
	/// @param player �Ʊ� / ����
	/// @param radius TilePosition ����
	public boolean hasBuildingAroundBaseLocation(BaseLocation baseLocation, Player player, int radius) {

		// invalid regions aren't considered the same, but they will both be null
		if (baseLocation == null) {
			return false;
		}

		// ������ 10 (TilePosition ����) �̸� ���� ȭ�� �����̴�
		int maxRadius = 10;

		if (unitData.get(player) != null) {
			Iterator<Integer> it = unitData.get(player).getUnitAndUnitInfoMap().keySet().iterator();

			// for (const auto & kv : _unitData[player].getUnits())
			while (it.hasNext()) {
				final UnitInfo ui = unitData.get(player).getUnitAndUnitInfoMap().get(it.next());
				if (ui.getType().isBuilding()) {
					TilePosition buildingPosition = ui.getLastPosition().toTilePosition();

					if (buildingPosition.getX() >= baseLocation.getTilePosition().getX() - maxRadius
							&& buildingPosition.getX() <= baseLocation.getTilePosition().getX() + maxRadius
							&& buildingPosition.getY() >= baseLocation.getTilePosition().getY() - maxRadius
							&& buildingPosition.getY() <= baseLocation.getTilePosition().getY() + maxRadius) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/// �ش� BaseLocation ���� 10Ÿ�� �ݰ� ���� player�� �ǹ��� �����ϴ��� �����մϴ�
	/// @param baseLocation ��� BaseLocation
	/// @param player �Ʊ� / ����
	public boolean hasBuildingAroundBaseLocation(BaseLocation baseLocation, Player player) {
		return hasBuildingAroundBaseLocation(baseLocation, player, 10);
	}

	/// �ش� Region �� �ش� Player�� �ǹ��� �����ϴ��� �����մϴ�
	public boolean existsPlayerBuildingInRegion(Region region, Player player) {
		// invalid regions aren't considered the same, but they will both be null
		if (region == null || player == null) {
			return false;
		}

		Iterator<Integer> it = unitData.get(player).getUnitAndUnitInfoMap().keySet().iterator();

		// for (const auto & kv : unitData.get(self).getUnits())
		while (it.hasNext()) {
			final UnitInfo ui = unitData.get(player).getUnitAndUnitInfoMap().get(it.next());
			if (ui.getType().isBuilding() ) {
				
				// Terran ������ Lifted �ǹ��� ���, BWTA.getRegion ����� null �̴�
				if (BWTA.getRegion(ui.getLastPosition()) == null) continue;

				if (BWTA.getRegion(ui.getLastPosition()) == region) {
					return true;
				}
			}
		}
		return false;
	}

	/// �ش� Player (�Ʊ� or ����) �� ��� ���� ��� (���� �ֱٰ�) UnitAndUnitInfoMap �� �����մϴ�		 
	/// �ľǵ� �������� �����ϱ� ������ ������ ������ Ʋ�� ���� �� �ֽ��ϴ�
	public final Map<Integer, UnitInfo> getUnitAndUnitInfoMap(Player player) {
		return getUnitData(player).getUnitAndUnitInfoMap();
	}

	/// �ش� Player (�Ʊ� or ����) �� �ǹ��� �Ǽ��ؼ� ������ Region ����� �����մϴ�
	public Set<Region> getOccupiedRegions(Player player) {
		return occupiedRegions.get(player);
	}

	/// �ش� Player (�Ʊ� or ����) �� �ǹ��� �Ǽ��ؼ� ������ BaseLocation ����� �����մϴ�		 
	public List<BaseLocation> getOccupiedBaseLocations(Player player) {
		return occupiedBaseLocations.get(player);
	}

	/// �ش� Player (�Ʊ� or ����) �� Main BaseLocation �� �����մϴ�		 
	public BaseLocation getMainBaseLocation(Player player) {
		return mainBaseLocations.get(player);
	}

	/// �ش� Player (�Ʊ� or ����) �� Main BaseLocation ���� ���� ����� ChokePoint �� �����մϴ�		 
	public Chokepoint getFirstChokePoint(Player player) {
		return firstChokePoint.get(player);
	}

	/// �ش� Player (�Ʊ� or ����) �� Main BaseLocation ���� ���� ����� Expansion BaseLocation �� �����մϴ�		 
	public BaseLocation getFirstExpansionLocation(Player player) {
		return firstExpansionLocation.get(player);
	}

	/// �ش� Player (�Ʊ� or ����) �� Main BaseLocation ���� �ι�°�� ����� ChokePoint �� �����մϴ�		 
	/// ���� �ʿ� ����, secondChokePoint �� �Ϲ� ��İ� �ٸ� ������ �� ���� �ֽ��ϴ�
	public Chokepoint getSecondChokePoint(Player player) {
		return secondChokePoint.get(player);
	}

	/// �ش� UnitType �� ���� �������� �����մϴ�
	public final boolean isCombatUnitType(UnitType type) {
		if (type == UnitType.Zerg_Lurker /* || type == UnitType.Protoss_Dark_Templar*/) {
			return false;
		}

		// check for various types of combat units
		if (type.canAttack() || type == UnitType.Terran_Medic || type == UnitType.Protoss_Observer
				|| type == UnitType.Terran_Bunker) {
			return true;
		}

		return false;
	}
	
	// �ش� ������ UnitType �� Basic Combat Unit �� �ش��ϴ� UnitType�� �����մϴ�
	public UnitType getBasicCombatUnitType() {
		return getBasicCombatUnitType(MyBotModule.Broodwar.self().getRace());
	}

	// �ش� ������ UnitType �� Basic Combat Unit �� �ش��ϴ� UnitType�� �����մϴ�
	public UnitType getBasicCombatUnitType(Race race) {
		if (race == Race.Protoss) {
			return UnitType.Protoss_Zealot;
		} else if (race == Race.Terran) {
			return UnitType.Terran_Marine;
		} else if (race == Race.Zerg) {
			return UnitType.Zerg_Zergling;
		} else {
			return UnitType.None;
		}
	}

	// �ش� ������ UnitType �� Advanced Combat Unit �� �ش��ϴ� UnitType�� �����մϴ�
	public UnitType getAdvancedCombatUnitType() {
		return getAdvancedCombatUnitType(MyBotModule.Broodwar.self().getRace());
	}

	// �ش� ������ UnitType �� Advanced Combat Unit �� �ش��ϴ� UnitType�� �����մϴ�
	public UnitType getAdvancedCombatUnitType(Race race) {
		if (race == Race.Protoss) {
			return UnitType.Protoss_Dragoon;
		} else if (race == Race.Terran) {
			return UnitType.Terran_Medic;
		} else if (race == Race.Zerg) {
			return UnitType.Zerg_Hydralisk;
		} else {
			return UnitType.None;
		}
	}

	// �ش� ������ UnitType �� Basic Combat Unit �� �����ϱ� ���� �Ǽ��ؾ��ϴ� UnitType�� �����մϴ�
	public UnitType getBasicCombatBuildingType() {
		return getBasicCombatBuildingType(MyBotModule.Broodwar.self().getRace());
	}

	// �ش� ������ UnitType �� Basic Combat Unit �� �����ϱ� ���� �Ǽ��ؾ��ϴ� UnitType�� �����մϴ�
	public UnitType getBasicCombatBuildingType(Race race) {
		if (race == Race.Protoss) {
			return UnitType.Protoss_Gateway;
		} else if (race == Race.Terran) {
			return UnitType.Terran_Barracks;
		} else if (race == Race.Zerg) {
			return UnitType.Zerg_Hatchery;
		} else {
			return UnitType.None;
		}
	}

	// �ش� ������ UnitType �� Observer �� �ش��ϴ� UnitType�� �����մϴ�
	public UnitType getObserverUnitType() {
		return getObserverUnitType(MyBotModule.Broodwar.self().getRace());
	}

	// �ش� ������ UnitType �� Observer �� �ش��ϴ� UnitType�� �����մϴ�
	public UnitType getObserverUnitType(Race race) {
		if (race == Race.Protoss) {
			return UnitType.Protoss_Observer;
		} else if (race == Race.Terran) {
			return UnitType.Terran_Science_Vessel;
		} else if (race == Race.Zerg) {
			return UnitType.Zerg_Overlord;
		} else {
			return UnitType.None;
		}
	}

	// �ش� ������ UnitType �� ResourceDepot ����� �ϴ� UnitType�� �����մϴ�
	public UnitType getBasicResourceDepotBuildingType() {
		return getBasicResourceDepotBuildingType(MyBotModule.Broodwar.self().getRace());
	}

	// �ش� ������ UnitType �� ResourceDepot ����� �ϴ� UnitType�� �����մϴ�
	public UnitType getBasicResourceDepotBuildingType(Race race) {
		if (race == Race.Protoss) {
			return UnitType.Protoss_Nexus;
		} else if (race == Race.Terran) {
			return UnitType.Terran_Command_Center;
		} else if (race == Race.Zerg) {
			return UnitType.Zerg_Hatchery;
		} else {
			return UnitType.None;
		}
	}

	// �ش� ������ UnitType �� Refinery ����� �ϴ� UnitType�� �����մϴ�
	public UnitType getRefineryBuildingType() {
		return getRefineryBuildingType(MyBotModule.Broodwar.self().getRace());
	}

	// �ش� ������ UnitType �� Refinery ����� �ϴ� UnitType�� �����մϴ�
	public UnitType getRefineryBuildingType(Race race) {
		if (race == Race.Protoss) {
			return UnitType.Protoss_Assimilator;
		} else if (race == Race.Terran) {
			return UnitType.Terran_Refinery;
		} else if (race == Race.Zerg) {
			return UnitType.Zerg_Extractor;
		} else {
			return UnitType.None;
		}
	}

	// �ش� ������ UnitType �� Worker �� �ش��ϴ� UnitType�� �����մϴ�
	public UnitType getWorkerType() {
		return getWorkerType(MyBotModule.Broodwar.self().getRace());
	}

	// �ش� ������ UnitType �� Worker �� �ش��ϴ� UnitType�� �����մϴ�
	public UnitType getWorkerType(Race race) {
		if (race == Race.Protoss) {
			return UnitType.Protoss_Probe;
		} else if (race == Race.Terran) {
			return UnitType.Terran_SCV;
		} else if (race == Race.Zerg) {
			return UnitType.Zerg_Drone;
		} else {
			return UnitType.None;
		}
	}

	// �ش� ������ UnitType �� SupplyProvider ����� �ϴ� UnitType�� �����մϴ�
	public UnitType getBasicSupplyProviderUnitType() {
		return getBasicSupplyProviderUnitType(MyBotModule.Broodwar.self().getRace());
	}

	// �ش� ������ UnitType �� SupplyProvider ����� �ϴ� UnitType�� �����մϴ�
	public UnitType getBasicSupplyProviderUnitType(Race race) {
		if (race == Race.Protoss) {
			return UnitType.Protoss_Pylon;
		} else if (race == Race.Terran) {
			return UnitType.Terran_Supply_Depot;
		} else if (race == Race.Zerg) {
			return UnitType.Zerg_Overlord;
		} else {
			return UnitType.None;
		}
	}

	// �ش� ������ UnitType �� Basic Depense ����� �ϴ� UnitType�� �����մϴ�
	public UnitType getBasicDefenseBuildingType() {
		return getBasicDefenseBuildingType(MyBotModule.Broodwar.self().getRace());
	}

	// �ش� ������ UnitType �� Basic Depense ����� �ϴ� UnitType�� �����մϴ�
	public UnitType getBasicDefenseBuildingType(Race race) {
		if (race == Race.Protoss) {
			return UnitType.Protoss_Pylon;
		} else if (race == Race.Terran) {
			return UnitType.Terran_Bunker;
		} else if (race == Race.Zerg) {
			return UnitType.Zerg_Creep_Colony;
		} else {
			return UnitType.None;
		}
	}

	// �ش� ������ UnitType �� Advanced Depense ����� �ϴ� UnitType�� �����մϴ�
	public UnitType getAdvancedDefenseBuildingType() {
		return getAdvancedDefenseBuildingType(MyBotModule.Broodwar.self().getRace());
	}

	// �ش� ������ UnitType �� Advanced Depense ����� �ϴ� UnitType�� �����մϴ�
	public UnitType getAdvancedDefenseBuildingType(Race race) {
		if (race == Race.Protoss) {
			return UnitType.Protoss_Photon_Cannon;
		} else if (race == Race.Terran) {
			return UnitType.Terran_Missile_Turret;
		} else if (race == Race.Zerg) {
			return UnitType.Zerg_Sunken_Colony;
		} else {
			return UnitType.None;
		}
	}
}