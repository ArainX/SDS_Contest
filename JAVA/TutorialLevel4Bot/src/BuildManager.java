import bwapi.Race;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;

public class BuildManager {

	private static BuildManager instance = new BuildManager();

	public static BuildManager Instance() {
		return instance;
	}

	public void update() {
		
		//constructBuildings();

		//buildCombatUnits();

		buildWorkerUnits();
	}
	
	
	private void buildWorkerUnits()
	{
		// �ڿ��� 50�̻� ������ �ϲ� ������ �Ʒ��Ѵ�
		if (MyBotModule.Broodwar.self().minerals() >= 50) {
			buildWorkerUnit();
		}
	}
	
	private void buildWorkerUnit()
	{
		Unit producer = null;
	
		UnitType targetUnitType = UnitType.None;
	
		if (MyBotModule.Broodwar.self().getRace() == Race.Protoss) {
			targetUnitType = UnitType.Protoss_Probe;
		}
		else if (MyBotModule.Broodwar.self().getRace() == Race.Terran) {
			targetUnitType = UnitType.Terran_SCV;
		}
		else if (MyBotModule.Broodwar.self().getRace() == Race.Zerg) {
			targetUnitType = UnitType.Zerg_Drone;
		}
			
		// ResourceDepot �ǹ��� �ϲ� ������ ���� ������ �����̸� ������ ����Ѵ�
		for (Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			if (unit.getType().isResourceDepot() ){
	
				if (MyBotModule.Broodwar.canMake(targetUnitType, unit) && unit.isTraining() == false && unit.isMorphing() == false) {
	
					producer = unit;
	
					if (MyBotModule.Broodwar.self().getRace() != Race.Zerg) {
						producer.train(targetUnitType);
					}
					else {
						producer.morph(targetUnitType);
					}
					break;
				}
			}
		}
	}
	
	private void buildCombatUnits()
	{
		UnitType targetUnitType = UnitType.None;
	
		// �ڿ��� 100�̻� ������ ���� ���� ������ �Ʒ��Ѵ�
		if (MyBotModule.Broodwar.self().minerals() >= 100) {
			if (MyBotModule.Broodwar.self().getRace() == Race.Protoss) {
				targetUnitType = UnitType.Protoss_Zealot;
			}
			else if (MyBotModule.Broodwar.self().getRace() == Race.Terran) {
				targetUnitType = UnitType.Terran_Marine;
			}
			else if (MyBotModule.Broodwar.self().getRace() == Race.Zerg) {
				targetUnitType = UnitType.Zerg_Zergling;
			}
	
			trainUnit(targetUnitType);
		}
	}
	
	private void trainUnit(UnitType targetUnitType)
	{
		Unit producer = null;
		UnitType producerUnitType = targetUnitType.whatBuilds().first;
	
		// targetUnitType�� ���� ������ ���°� �Ǹ� ������ ����Ѵ�
		for (Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			if (unit.getType() == producerUnitType) {
				if (MyBotModule.Broodwar.canMake(targetUnitType, unit) && unit.isTraining() == false && unit.isMorphing() == false) {
	
					producer = unit;
	
					if (MyBotModule.Broodwar.self().getRace() != Race.Zerg) {
						producer.train(targetUnitType);
					}
					else {
						producer.morph(targetUnitType);
					}
					break;
				}
			}
		}
	}
	
	
	private void constructBuildings()
	{
		UnitType targetUnitType = UnitType.None;
	
		// �ڿ��� 200�̻� ������ �������� ���� �ǹ��� �Ǽ� �Ѵ�
		if (MyBotModule.Broodwar.self().minerals() >= 200) {
			if (MyBotModule.Broodwar.self().getRace() == Race.Protoss) {
				targetUnitType = UnitType.Protoss_Gateway;
			}
			else if (MyBotModule.Broodwar.self().getRace() == Race.Terran) {
				targetUnitType = UnitType.Terran_Barracks;
			}
			else if (MyBotModule.Broodwar.self().getRace() == Race.Zerg) {
				targetUnitType = UnitType.Zerg_Spawning_Pool;
			}
			constructBuilding(targetUnitType);
		}
	
		// �ڿ��� 100�̻� �ְ�, ���ö��̰� ���������� SupplyProvider �� �ش��ϴ� ������ �����
		// ���ö��� ���ڴ� ��Ÿũ����Ʈ ���ӿ��� ǥ�õǴ� ������ 2��� ����ؾ��Ѵ�
		if (MyBotModule.Broodwar.self().minerals() >= 100
			&& MyBotModule.Broodwar.self().supplyUsed() + 6 > MyBotModule.Broodwar.self().supplyTotal()) {
			if (MyBotModule.Broodwar.self().getRace() == Race.Protoss) {
				targetUnitType = UnitType.Protoss_Pylon;
				constructBuilding(targetUnitType);
			}
			else if (MyBotModule.Broodwar.self().getRace() == Race.Terran) {
				targetUnitType = UnitType.Terran_Supply_Depot;
				constructBuilding(targetUnitType);
			}
			else if (MyBotModule.Broodwar.self().getRace() == Race.Zerg) {
				targetUnitType = UnitType.Zerg_Overlord;
				trainUnit(targetUnitType);
			}
		}
	}
	
	private void constructBuilding(UnitType targetBuildingType)
	{
		// �ϲ� �� �̳׶��� ����ϰ� ���� ���� �ϲ� �ϳ��� producer�� �����Ѵ�
		Unit producer = null;
		UnitType producerUnitType = targetBuildingType.whatBuilds().first;
	
		for (Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			if (unit.getType() == producerUnitType) {
				if (MyBotModule.Broodwar.canMake(targetBuildingType, unit)
					&& unit.isCompleted() 
					&& unit.isCarryingMinerals() == false
					&& unit.isConstructing() == false) {
	
					producer = unit;
					break;
				}
			}
		}
	
		if (producer == null) {
			return;
		}
	
		// �ǹ��� �Ǽ��� ��ġ�� Start Location ��ó���� ã�´�
		// ó������ Start Location �ݰ� 4Ÿ�Ͽ� ���� ã�ƺ���, 
		// �������� Start Location �ݰ� 8Ÿ�Ͽ� ���� ã�ƺ��� ������ ������ ����������
		TilePosition seedPosition = InformationManager.Instance().mainBaseLocations.get(InformationManager.Instance().selfPlayer).getTilePosition();
		TilePosition desiredPosition = TilePosition.None;
		int maxRange = 32;
		boolean constructionPlaceFound = false;
	
		for (int range = 4; range <= maxRange; range *= 2) {
			for (int i = seedPosition.getX() - range; i < seedPosition.getX() + range; i++) {
				for (int j = seedPosition.getY() - range; j < seedPosition.getY() + range; j++) {
					desiredPosition = new TilePosition(i,j);
					if (MyBotModule.Broodwar.canBuildHere(desiredPosition, targetBuildingType, producer, true))	{
						constructionPlaceFound = true;
						break;
					}
				}
				if (constructionPlaceFound) break;
			}
			if (constructionPlaceFound) break;
		}
	
		if (constructionPlaceFound == true && desiredPosition != TilePosition.None) {
			producer.build(targetBuildingType, desiredPosition);
		}
	}
	
	

	
};