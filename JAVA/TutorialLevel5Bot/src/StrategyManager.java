import bwapi.Race;
import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitCommand;
import bwapi.UnitType;
import bwapi.UpgradeType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;

/// ��Ȳ�� �Ǵ��Ͽ�, ����, ����, ����, ��� ���� �����ϵ��� �Ѱ� ���ָ� �ϴ� class
/// InformationManager �� �ִ� ������κ��� ��Ȳ�� �Ǵ��ϰ�, 
/// BuildManager �� buildQueue�� ���� (�ǹ� �Ǽ� / ���� �Ʒ� / ��ũ ����ġ / ���׷��̵�) ����� �Է��մϴ�.
/// ����, ����, ����, ��� ���� �����ϴ� �ڵ尡 ���� class
public class StrategyManager {

	private static StrategyManager instance = new StrategyManager();

	private CommandUtil commandUtil = new CommandUtil();

	private boolean isFullScaleAttackStarted;
	private boolean isInitialBuildOrderFinished;

	/// static singleton ��ü�� �����մϴ�
	public static StrategyManager Instance() {
		return instance;
	}

	public StrategyManager() {
		isFullScaleAttackStarted = false;
		isInitialBuildOrderFinished = false;
	}

	/// ��Ⱑ ���۵� �� ��ȸ������ ���� �ʱ� ���� ���� ������ �����մϴ�
	public void onStart() {
		setInitialBuildOrder();		
	}

	///  ��Ⱑ ����� �� ��ȸ������ ���� ��� ���� ���� ������ �����մϴ�
	public void onEnd(boolean isWinner) {

	}

	/// ��� ���� �� �� �����Ӹ��� ��� ���� ���� ������ �����մϴ�
	public void update() {
		if (BuildManager.Instance().buildQueue.isEmpty()) {
			isInitialBuildOrderFinished = true;
		}

		executeWorkerTraining();

		executeSupplyManagement();

		executeBasicCombatUnitTraining();

		executeCombat();
	}

	public void setInitialBuildOrder() {

		if (MyBotModule.Broodwar.self().getRace() == Race.Protoss) {

			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType());
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType());
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType());

			// SupplyUsed�� 7 �϶� ���Ϸ� ����
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getBasicSupplyProviderUnitType(), BuildOrderItem.SeedPositionStrategy.MainBaseLocation);

			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType());

			// SupplyUsed�� 8 �϶� 1��° ����Ʈ���� ����
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Gateway, BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
			
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType());

			// SupplyUsed�� 9 �϶� ���� �����̳ʸ� ����
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getRefineryBuildingType());

			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType());

			// SupplyUsed�� 10 �϶� ���̹���ƽ�� �ھ� ����
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Cybernetics_Core, BuildOrderItem.SeedPositionStrategy.MainBaseLocation);

			// 1��° ���� ����
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Zealot);

			// SupplyUsed�� 12 �϶� ��Ÿ�� ���� �Ƶ� ����
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Citadel_of_Adun);

			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType());
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType());

			// SupplyUsed�� 14 �϶� ���÷� ��ī�̺�, 2��° ����Ʈ���� ����
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Templar_Archives);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Gateway, BuildOrderItem.SeedPositionStrategy.MainBaseLocation);

			// 2��° ���� ����
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Zealot);

			// SupplyUsed�� 16 �϶� ���Ϸ� ����
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getBasicSupplyProviderUnitType(), BuildOrderItem.SeedPositionStrategy.MainBaseLocation);

			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType());

			// 4���� ��ũ ���÷� ���� �� ���Ϸ� ����
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Dark_Templar);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Dark_Templar);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Dark_Templar);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Dark_Templar);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getBasicSupplyProviderUnitType(), BuildOrderItem.SeedPositionStrategy.MainBaseLocation);

		}
	}

	// �ϲ� ��� �߰� ����
	public void executeWorkerTraining() {

		// InitialBuildOrder �����߿��� �ƹ��͵� ���� �ʽ��ϴ�
		if (isInitialBuildOrderFinished == false) {
			return;
		}

		if (MyBotModule.Broodwar.self().minerals() >= 50) {
			// workerCount = ���� �ϲ� �� + �������� �ϲ� ��
			int workerCount = MyBotModule.Broodwar.self().allUnitCount(InformationManager.Instance().getWorkerType());

			if (MyBotModule.Broodwar.self().getRace() == Race.Zerg) {
				for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
					if (unit.getType() == UnitType.Zerg_Egg) {
						// Zerg_Egg ���� morph ����� ������ isMorphing = true,
						// isBeingConstructed = true, isConstructing = true �� �ȴ�
						// Zerg_Egg �� �ٸ� �������� �ٲ�鼭 ���� ������� ������ ���
						// isBeingConstructed = true, isConstructing = true ��
						// �Ǿ��ٰ�,
						if (unit.isMorphing() && unit.getBuildType() == UnitType.Zerg_Drone) {
							workerCount++;
						}
					}
				}
			} else {
				for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
					if (unit.getType().isResourceDepot()) {
						if (unit.isTraining()) {
							workerCount += unit.getTrainingQueue().size();
						}
					}
				}
			}

			if (workerCount < 30) {
				for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
					if (unit.getType().isResourceDepot()) {
						if (unit.isTraining() == false || unit.getLarva().size() > 0) {
							// ����ť�� �ϲ� ������ 1���� �ֵ��� �Ѵ�
							if (BuildManager.Instance().buildQueue
									.getItemCount(InformationManager.Instance().getWorkerType(), null) == 0) {
								// std.cout << "worker enqueue" << std.endl;
								BuildManager.Instance().buildQueue.queueAsLowestPriority(
										new MetaType(InformationManager.Instance().getWorkerType()), false);
							}
						}
					}
				}
			}
		}
	}

	// Supply DeadLock ���� �� SupplyProvider �� �������� ��Ȳ �� ���� ������ �������μ�
	// SupplyProvider�� �߰� �Ǽ�/�����Ѵ�
	public void executeSupplyManagement() {

		// InitialBuildOrder �����߿��� �ƹ��͵� ���� �ʽ��ϴ�
		if (isInitialBuildOrderFinished == false) {
			return;
		}

		// 1�ʿ� �ѹ��� ����
		if (MyBotModule.Broodwar.getFrameCount() % 24 != 0) {
			return;
		}

		// ���ӿ����� ���ö��� ���� 200���� ������, BWAPI ������ ���ö��� ���� 400���� �ִ�
		// ���۸� 1������ ���ӿ����� ���ö��̸� 0.5 ����������, BWAPI ������ ���ö��̸� 1 �����Ѵ�
		if (MyBotModule.Broodwar.self().supplyTotal() <= 400) {

			// ���ö��̰� �� ��á���� �� ���ö��̸� ������ ������ ���� �Ͼ�Ƿ�, supplyMargin (���ӿ����� ���ö��� ���� ���� 2��)��ŭ ���������� �� ���ö��̸� ������ �Ѵ�
			// �̷��� ���� ���س�����, ���� �ʹݺο��� ���ö��̸� �ʹ� ���� ����, ���� �Ĺݺο��� ���ö��̸� �ʹ� �ʰ� ���� �ȴ�
			int supplyMargin = 12;

			// currentSupplyShortage �� ����Ѵ�
			int currentSupplyShortage = MyBotModule.Broodwar.self().supplyUsed() + supplyMargin - MyBotModule.Broodwar.self().supplyTotal();

			if (currentSupplyShortage > 0) {
				
				// ����/�Ǽ� ���� Supply�� ����
				int onBuildingSupplyCount = 0;

				// ���� ������ ���, �������� Zerg_Overlord (Zerg_Egg) �� ����. Hatchery �� �ǹ��� ���� �ʴ´�
				if (MyBotModule.Broodwar.self().getRace() == Race.Zerg) {
					for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
						if (unit.getType() == UnitType.Zerg_Egg && unit.getBuildType() == UnitType.Zerg_Overlord) {
							onBuildingSupplyCount += UnitType.Zerg_Overlord.supplyProvided();
						}
						// ���¾ Overlord �� ���� SupplyTotal �� �ݿ��ȵǾ, �߰� ī��Ʈ�� �������
						if (unit.getType() == UnitType.Zerg_Overlord && unit.isConstructing()) {
							onBuildingSupplyCount += UnitType.Zerg_Overlord.supplyProvided();
						}
					}
				}
				// ���� ������ �ƴ� ���, �Ǽ����� Protoss_Pylon, Terran_Supply_Depot �� ����. Nexus, Command Center �� �ǹ��� ���� �ʴ´�
				else {
					onBuildingSupplyCount += ConstructionManager.Instance().getConstructionQueueItemCount(
							InformationManager.Instance().getBasicSupplyProviderUnitType(), null)
							* InformationManager.Instance().getBasicSupplyProviderUnitType().supplyProvided();
				}

				//System.out.println("currentSupplyShortage : " + currentSupplyShortage + " onBuildingSupplyCount : " + onBuildingSupplyCount);

				if (currentSupplyShortage > onBuildingSupplyCount) {
					
					// BuildQueue �ֻ�ܿ� SupplyProvider �� ���� ������ enqueue �Ѵ�
					boolean isToEnqueue = true;
					if (!BuildManager.Instance().buildQueue.isEmpty()) {
						BuildOrderItem currentItem = BuildManager.Instance().buildQueue.getHighestPriorityItem();
						if (currentItem.metaType.isUnit() 
							&& currentItem.metaType.getUnitType() == InformationManager.Instance().getBasicSupplyProviderUnitType()) 
						{
							isToEnqueue = false;
						}
					}
					if (isToEnqueue) {
						System.out.println("enqueue supply provider "
								+ InformationManager.Instance().getBasicSupplyProviderUnitType());
						BuildManager.Instance().buildQueue.queueAsHighestPriority(
								new MetaType(InformationManager.Instance().getBasicSupplyProviderUnitType()), true);
					}
				}
			}
		}
	}

	public void executeBasicCombatUnitTraining() {

		// InitialBuildOrder �����߿��� �ƹ��͵� ���� �ʽ��ϴ�
		if (isInitialBuildOrderFinished == false) {
			return;
		}

		// �⺻ ���� �߰� �Ʒ�
		if (MyBotModule.Broodwar.self().minerals() >= 200 && MyBotModule.Broodwar.self().supplyUsed() < 390) {
			for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
				if (unit.getType() == InformationManager.Instance().getBasicCombatBuildingType()) {
					if (unit.isTraining() == false || unit.getLarva().size() > 0) {
						if (BuildManager.Instance().buildQueue
								.getItemCount(InformationManager.Instance().getBasicCombatUnitType(), null) == 0) {
							BuildManager.Instance().buildQueue.queueAsLowestPriority(
									InformationManager.Instance().getBasicCombatUnitType(),
									BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
						}
					}
				}
			}
		}
	}

	public void executeCombat() {

		// ���� ��尡 �ƴ� ������ �������ֵ��� �Ʊ� ���� ��� ������Ѽ� ���
		if (isFullScaleAttackStarted == false) {
			Chokepoint firstChokePoint = BWTA.getNearestChokepoint(InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer).getTilePosition());

			for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
				if (unit.getType() == InformationManager.Instance().getBasicCombatUnitType() && unit.isIdle()) {
					commandUtil.attackMove(unit, firstChokePoint.getCenter());
				}
			}

			// Protoss_Dark_Templar ������ 2�� �̻� ����Ǿ���, ���� ��ġ�� �ľǵǾ����� �Ѱ��� ���� ��ȯ
			if (MyBotModule.Broodwar.self().completedUnitCount(UnitType.Protoss_Dark_Templar) >= 2) {
				if (InformationManager.Instance().enemyPlayer != null
					&& InformationManager.Instance().enemyRace != Race.Unknown  
					&& InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().enemyPlayer).size() > 0) {				
					isFullScaleAttackStarted = true;
				}
			}
		}
		// ���� ��尡 �Ǹ�, ��� �������ֵ��� ���� Main BaseLocation �� ���� ������ �մϴ�
		else {
			//std.cout << "enemy OccupiedBaseLocations : " << InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance()._enemy).size() << std.endl;
			
			if (InformationManager.Instance().enemyPlayer != null
					&& InformationManager.Instance().enemyRace != Race.Unknown 
					&& InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().enemyPlayer).size() > 0) 
			{					
				// ���� ��� ���� ����
				BaseLocation targetBaseLocation = null;
				double closestDistance = 100000000;

				for (BaseLocation baseLocation : InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().enemyPlayer)) {
					double distance = BWTA.getGroundDistance(
						InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer).getTilePosition(), 
						baseLocation.getTilePosition());

					if (distance < closestDistance) {
						closestDistance = distance;
						targetBaseLocation = baseLocation;
					}
				}

				if (targetBaseLocation != null) {
					for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
						// �ǹ��� ����
						if (unit.getType().isBuilding()) {
							continue;
						}
						// ��� �ϲ��� ����
						if (unit.getType().isWorker()) {
							continue;
						}
											
						// canAttack ������ attackMove Command �� ������ �����ϴ�
						if (unit.canAttack()) {
							
							if (unit.isIdle()) {
								commandUtil.attackMove(unit, targetBaseLocation.getPosition());
							}
						} 
					}
				}
			}
		}
	}
}