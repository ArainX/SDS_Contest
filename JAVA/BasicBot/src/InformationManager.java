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

	public List<Unit> validUnits = new ArrayList<Unit>();

	private Map<Player, BaseLocation> mainBaseLocations = new HashMap<Player, BaseLocation>();
	private Map<Player, List<BaseLocation>> occupiedBaseLocations = new HashMap<Player, List<BaseLocation>>();
	private Map<Player, Set<Region>> occupiedRegions = new HashMap<Player, Set<Region>>();

	private Map<Player, Chokepoint> firstChokePoint = new HashMap<Player, Chokepoint>();
	private Map<Player, BaseLocation> firstExpansionLocation = new HashMap<Player, BaseLocation>();
	private Map<Player, Chokepoint> secondChokePoint = new HashMap<Player, Chokepoint>();

	/// Player - UnitData(�� Unit �� �� Unit�� UnitInfo �� Map ���·� �����ϴ� �ڷᱸ��) �� �����ϴ� �ڷᱸ�� ��ü
	private Map<Player, UnitData> unitData = new HashMap<Player, UnitData>();

	/// static singleton ��ü�� �����մϴ�
	public static InformationManager Instance() {
		return instance;
	}

	public Player getSelf() {
		return selfPlayer;
	}

	public InformationManager() {
		selfPlayer = MyBotModule.Broodwar.self();
		enemyPlayer = MyBotModule.Broodwar.enemy();
		selfRace = selfPlayer.getRace();
		enemyRace = enemyPlayer.getRace();
		
		mainBaseLocations = new HashMap<Player, BaseLocation>();
		mainBaseLocations.put(selfPlayer, BWTA.getStartLocation(MyBotModule.Broodwar.self()));
		occupiedBaseLocations = new HashMap<Player, List<BaseLocation>>();
		occupiedBaseLocations.put(selfPlayer, new ArrayList<BaseLocation>());
		occupiedBaseLocations.get(selfPlayer).add(mainBaseLocations.get(selfPlayer));
		updateOccupiedRegions(BWTA.getRegion(mainBaseLocations.get(selfPlayer).getTilePosition()),
				MyBotModule.Broodwar.self());
		mainBaseLocations.put(enemyPlayer, null);
		occupiedBaseLocations.put(enemyPlayer, new ArrayList<BaseLocation>());
		firstChokePoint.put(selfPlayer, null);
		firstChokePoint.put(enemyPlayer, null);
		firstExpansionLocation.put(selfPlayer, null);
		firstExpansionLocation.put(enemyPlayer, null);
		secondChokePoint.put(selfPlayer, null);
		secondChokePoint.put(enemyPlayer, null);
		updateChokePointAndExpansionLocation();
		occupiedRegions.put(selfPlayer, new HashSet());
		occupiedRegions.put(enemyPlayer, new HashSet());
	}

	/// Unit �� BaseLocation, ChokePoint � ���� ������ ������Ʈ�մϴ�
	public void update() {
		// ������ Eliminate �ǰų� Left ���� ��, enemy ���� �ڵ����� null �� ���� �����Ƿ�, null ��
		// �������� �Ѵ�
		if (MyBotModule.Broodwar.enemy() == null) {
			enemyPlayer = null;
		} else {
			if (MyBotModule.Broodwar.enemy().isDefeated() || MyBotModule.Broodwar.enemy().leftGame()) {
				enemyPlayer = null;
			}
		}

		updateUnitsInfo();
		// occupiedBaseLocation �̳� occupiedRegion �� ���� �ȹٲ�Ƿ� ���� ���ص� �ȴ�
		if (MyBotModule.Broodwar.getFrameCount() % 120 == 0) {
			updateBaseLocationInfo();
		}
	}

	/// ��ü unit �� ������ ������Ʈ �մϴ� (UnitType, lastPosition, HitPoint ��)
	public void updateUnitsInfo() {
		// update our units info

		validUnits.clear();
		setValidUnits();
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

	// validates units as usable for distribution to various managers
	public void setValidUnits() {
		// make sure the unit is completed and alive and usable
		for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
			if (unit == null) {
				continue;
			}

			if (unit.isCompleted() && unit.getHitPoints() > 0 && unit.exists() && unit.getType() != UnitType.Unknown
					&& unit.getPosition() != Position.Unknown) {
				validUnits.add(unit);
			}
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
			if (unitData.get(unit.getPlayer()) == null) {
				unitData.put(unit.getPlayer(), new UnitData());
			}
			unitData.get(unit.getPlayer()).updateUnitInfo(unit);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// is the unit valid?
	boolean isValidUnit(Unit unit) {
		// we only care about our units and enemy units
		if (unit.getPlayer() != MyBotModule.Broodwar.self() && unit.getPlayer() != MyBotModule.Broodwar.enemy()) {
			return false;
		}

		// if it's a weird unit, don't bother
		if (unit.getType() == UnitType.None || unit.getType() == UnitType.Unknown
				|| unit.getType() == UnitType.Zerg_Larva || unit.getType() == UnitType.Zerg_Egg) {
			return false;
		}

		// if the position isn't valid throw it out
		if (!unit.getPosition().isValid()) {
			return false;
		}

		// s'all good baby baby
		return true;
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
		Iterator<Unit> it = getUnitData(player).getUnits().keySet().iterator();

		// for each unit we know about for that player
		// for (final Unit kv :
		// getUnitData(player).getUnits().keySet().iterator()){
		while (it.hasNext()) {
			Unit unit = it.next();
			final UnitInfo ui = getUnitData(player).getUnits().get(unit);

			// if it's a combat unit we care about
			// and it's finished!
			if (isCombatUnit(ui.getType()) && ui.isCompleted()) {
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

	public Unit getClosestUnitToTarget(UnitType type, Position target) {
		Unit closestUnit = null;
		double closestDist = 100000;

		for (Unit unit : validUnits) {
			if (unit.getType() == type) {
				double dist = unit.getDistance(target);
				if (closestUnit == null || dist < closestDist) {
					closestUnit = unit;
					closestDist = dist;
				}
			}
		}

		return closestUnit;
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
				if (existsEnemyBuildingInRegion(BWTA.getRegion(startLocation.getTilePosition()))) {
					if (enemyStartLocationFound == false) {
						enemyStartLocationFound = true;
						mainBaseLocations.put(enemyPlayer, startLocation);
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
			if (existsEnemyBuildingInRegion(BWTA.getRegion(mainBaseLocations.get(enemyPlayer).getTilePosition())) == false) {
				for (BaseLocation loaction : occupiedBaseLocations.get(enemyPlayer)) {
					if (existsEnemyBuildingInRegion(BWTA.getRegion(loaction.getTilePosition()))) {
						mainBaseLocations.put(enemyPlayer, loaction);
						break;
					}
				}
			}
		}

		// self�� mainBaseLocations�� ����, �װ��� �ִ� �ǹ��� ��� �ı��� ���
		// _occupiedBaseLocations �߿��� _mainBaseLocations �� �����Ѵ�
		if (mainBaseLocations.get(selfPlayer) != null) {
			if (existsEnemyBuildingInRegion(BWTA.getRegion(mainBaseLocations.get(selfPlayer).getTilePosition())) == false) {
				for (BaseLocation loaction : occupiedBaseLocations.get(selfPlayer)) {
					if (existsEnemyBuildingInRegion(BWTA.getRegion(loaction.getTilePosition()))) {
						mainBaseLocations.put(selfPlayer, loaction);
						break;
					}
				}
			}
		}

		Iterator<Unit> it = null;
		if (unitData.get(enemyPlayer) != null) {
			it = unitData.get(enemyPlayer).getUnits().keySet().iterator();

			// for each enemy building unit we know about
			// for (const auto & kv : unitData.get(enemy).getUnits())
			while (it.hasNext()) {
				Unit unit = it.next();
				final UnitInfo ui = unitData.get(enemyPlayer).getUnits().get(unit);
				if (ui.getType().isBuilding()) {
					updateOccupiedRegions(BWTA.getRegion(ui.getLastPosition().toTilePosition()),
							MyBotModule.Broodwar.enemy());
				}
			}
		}

		if (unitData.get(selfPlayer) != null) {
			it = unitData.get(selfPlayer).getUnits().keySet().iterator();

			// for each of our building units
			// for (const auto & kv : _unitData[_self].getUnits())
			while (it.hasNext()) {
				Unit unit = it.next();
				final UnitInfo ui = unitData.get(selfPlayer).getUnits().get(unit);
				if (ui.getType().isBuilding()) {
					updateOccupiedRegions(BWTA.getRegion(ui.getLastPosition().toTilePosition()),
							MyBotModule.Broodwar.self());
				}
			}
		}

		updateChokePointAndExpansionLocation();
	}

	public void updateChokePointAndExpansionLocation() {
		if (mainBaseLocations.get(selfPlayer) != null) {
			BaseLocation sourceBaseLocation = mainBaseLocations.get(selfPlayer);

			// �� �߾� Ȥ�� ����� ������ �������� FirstChokePoint, SecondChokePoint ����
			Position targetPosition = new Position(MyBotModule.Broodwar.mapWidth() * Config.TILE_SIZE / 2,
					MyBotModule.Broodwar.mapHeight() * Config.TILE_SIZE / 2);
			if (mainBaseLocations.get(enemyPlayer) != null) {
				targetPosition = mainBaseLocations.get(enemyPlayer).getPosition();
			}

			if (BWTA.isConnected(sourceBaseLocation.getTilePosition(), targetPosition.toTilePosition())) {

				firstChokePoint.put(selfPlayer, BWTA.getNearestChokepoint(sourceBaseLocation.getTilePosition()));

				if (firstChokePoint.get(selfPlayer) != null) {

					firstExpansionLocation.put(selfPlayer,
							BWTA.getNearestBaseLocation(firstChokePoint.get(selfPlayer).getCenter().toTilePosition()));

					if (firstExpansionLocation.get(selfPlayer) != null) {
						double tempDistance;
						double closestDistance = 100000000;
						TilePosition tempTilePosition;
						for (Chokepoint chokePoint : BWTA.getChokepoints()) {
							if (chokePoint != BWTA.getNearestChokepoint(sourceBaseLocation.getTilePosition())) {
								tempDistance = BWTA.getGroundDistance(chokePoint.getCenter().toTilePosition(),
										firstExpansionLocation.get(selfPlayer).getTilePosition());
								if (tempDistance < closestDistance) {
									closestDistance = tempDistance;
									secondChokePoint.put(selfPlayer, chokePoint);
								}
							}
						}
					}
				}
			}
		}

		if (mainBaseLocations.get(enemyPlayer) != null) {
			BaseLocation sourceBaseLocation = mainBaseLocations.get(enemyPlayer);

			// �� �߾� Ȥ�� ����� ������ �������� FirstChokePoint, SecondChokePoint ����
			Position targetPosition = new Position(MyBotModule.Broodwar.mapWidth() * Config.TILE_SIZE / 2,
					MyBotModule.Broodwar.mapHeight() * Config.TILE_SIZE / 2);
			if (mainBaseLocations.get(selfPlayer) != null) {
				targetPosition = mainBaseLocations.get(selfPlayer).getPosition();
			}

			if (BWTA.isConnected(sourceBaseLocation.getTilePosition(), targetPosition.toTilePosition())) {

				firstChokePoint.put(enemyPlayer, BWTA.getNearestChokepoint(sourceBaseLocation.getTilePosition()));

				if (firstChokePoint.get(enemyPlayer) != null) {

					firstExpansionLocation.put(enemyPlayer,
							BWTA.getNearestBaseLocation(firstChokePoint.get(enemyPlayer).getCenter()));

					if (firstExpansionLocation.get(enemyPlayer) != null) {
						double tempDistance;
						double closestDistance = 100000000;
						TilePosition tempTilePosition;
						for (Chokepoint chokePoint : BWTA.getChokepoints()) {
							if (chokePoint != BWTA.getNearestChokepoint(sourceBaseLocation.getTilePosition())) {
								tempDistance = BWTA.getGroundDistance(chokePoint.getCenter().toTilePosition(),
										firstExpansionLocation.get(enemyPlayer).getTilePosition());
								if (tempDistance < closestDistance) {
									closestDistance = tempDistance;
									secondChokePoint.put(enemyPlayer, chokePoint);
								}
							}
						}
					}
				}
			}
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
			Iterator<Unit> it = unitData.get(player).getUnits().keySet().iterator();

			// for (const auto & kv : _unitData[player].getUnits())
			while (it.hasNext()) {
				Unit unit = it.next();
				final UnitInfo ui = unitData.get(player).getUnits().get(unit);
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


	/// �ش� Region �� ���� �ǹ��� �����ϴ��� �����մϴ�
	public boolean existsEnemyBuildingInRegion(Region region) {
		// invalid regions aren't considered the same, but they will both be
		// null
		if (region == null) {
			return false;
		}

		if (unitData.get(enemyPlayer) != null) {
			Iterator<Unit> it = unitData.get(enemyPlayer).getUnits().keySet().iterator();

			// for (const auto & kv : unitData[_enemy].getUnits())
			while (it.hasNext()) {
				Unit unit = it.next();
				final UnitInfo ui = unitData.get(enemyPlayer).getUnits().get(unit);
				if (ui.getType().isBuilding()) {
					if (BWTA.getRegion(ui.getLastPosition()) == region) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/// �ش� Region �� �Ʊ� �ǹ��� �����ϴ��� �����մϴ�
	public boolean existsMyBuildingInRegion(Region region) {
		// invalid regions aren't considered the same, but they will both be
		// null
		if (region == null) {
			return false;
		}

		Iterator<Unit> it = unitData.get(selfPlayer).getUnits().keySet().iterator();

		// for (const auto & kv : unitData.get(self).getUnits())
		while (it.hasNext()) {
			Unit unit = it.next();
			final UnitInfo ui = unitData.get(selfPlayer).getUnits().get(unit);
			if (ui.getType().isBuilding() && ui.isCompleted()) {
				if (BWTA.getRegion(ui.getLastPosition()) == region) {
					return true;
				}
			}
		}

		return false;
	}

	/// �ش� Player (�Ʊ� or ����) �� ��� ���� ��� (���� �ֱٰ�) UnitAndUnitInfoMap �� �����մϴ�		 
	/// �ľǵ� �������� �����ϱ� ������ ������ ������ Ʋ�� ���� �� �ֽ��ϴ�
	public final Map<Unit, UnitInfo> getUnitInfo(Player player) {
		return getUnitData(player).getUnits();
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
	public Chokepoint getSecondChokePoint(Player player) {
		return secondChokePoint.get(player);
	}

	/// �ش� UnitType �� ���� �������� �����մϴ�
	public final boolean isCombatUnit(UnitType type) {
		if (type == UnitType.Zerg_Lurker/*
										 * || type == BWAPI::UnitTypes::
										 * Protoss_Dark_Templar
										 */) {
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