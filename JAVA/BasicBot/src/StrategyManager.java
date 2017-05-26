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
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(
					InformationManager.Instance().getBasicSupplyProviderUnitType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Gateway,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Zealot,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);

			/*
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Assimilator,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation);

			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Forge,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Photon_Cannon,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation);

			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Gateway,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Zealot);

			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Cybernetics_Core,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Dragoon);
			// ��� �����Ÿ� ���׷��̵�
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Singularity_Charge);

			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Citadel_of_Adun);
			// ���� �ӵ� ���׷��̵�
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Leg_Enhancements);

			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Shield_Battery);

			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Templar_Archives);
			// �������÷�
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_High_Templar);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_High_Templar);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Psionic_Storm);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Hallucination);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Khaydarin_Amulet);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Archon);

			// ��ũ��ĭ
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Dark_Templar);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Dark_Templar);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Maelstrom);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Mind_Control);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Argus_Talisman);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Dark_Archon);

			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Robotics_Facility);

			// ��Ʋ
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Shuttle);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Robotics_Support_Bay);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Gravitic_Drive);

			// ����
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Reaver);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Scarab_Damage);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Reaver_Capacity);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Scarab);

			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Observatory);
			// ������
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Observer);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Gravitic_Boosters);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Sensor_Array);

			// ��������
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Stargate);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Fleet_Beacon);

			// ��ī��Ʈ
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Scout);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Apial_Sensors);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Gravitic_Thrusters);

			// Ŀ����
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Corsair);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Disruption_Web);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Argus_Jewel);

			// ĳ����
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Carrier);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Carrier_Capacity);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Interceptor);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Interceptor);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Interceptor);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Interceptor);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Interceptor);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Interceptor);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Interceptor);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Interceptor);

			// �ƺ���
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Arbiter_Tribunal);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Arbiter);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Recall);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Stasis_Field);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Khaydarin_Core);

			// ���� - ���� ���� ���׷��̵�
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Protoss_Ground_Weapons);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Protoss_Plasma_Shields);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Protoss_Ground_Armor);

			// ���̹���ƽ���ھ� - ���� ���� ���׷��̵�
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Protoss_Air_Weapons);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Protoss_Air_Armor);

			*/
		} 
		else if (MyBotModule.Broodwar.self().getRace() == Race.Terran) {
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(
					InformationManager.Instance().getBasicSupplyProviderUnitType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Barracks,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);

			/*
			// ���� �����̳ʸ�
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getRefineryBuildingType());

			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Barracks,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Bunker,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine);

			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Academy);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Comsat_Station);

			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Medic);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Firebat);

			// �������� ���׷��̵�
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Engineering_Bay);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Terran_Infantry_Weapons, false);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Terran_Infantry_Armor, false);

			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Missile_Turret);

			// ���� ������
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Stim_Packs, false);
			// ���� �����Ÿ� ��
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.U_238_Shells, false);

			// �޵�
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Optical_Flare, false);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Restoration, false);
			// �޵� ������ ��
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Caduceus_Reactor, false);

			// ���丮
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Machine_Shop);
			// ���� �����̴� ����
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Spider_Mines, false);
			// ���� �̵��ӵ� ��
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Ion_Thrusters, false);
			// ������ũ ������
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Siege_Tank_Tank_Mode);

			// ����
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Vulture);

			// ������ũ
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Tank_Siege_Mode, false);

			// �ƸӴ�
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Armory);
			// ���� ��ī�� ���� ���׷��̵�
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Terran_Vehicle_Plating, false);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Terran_Vehicle_Weapons, false);
			// ���� ���� ���׷��̵�
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Terran_Ship_Plating, false);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Terran_Ship_Weapons, false);
			// �񸮾� �����Ÿ� ��
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Charon_Boosters, false);

			// �񸮾�
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Goliath);

			// ��Ÿ��Ʈ
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Starport);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Control_Tower);
			// ���̾� Ŭ��ŷ
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Cloaking_Field, false);
			// ���̾� ������ ��
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Apollo_Reactor, false);

			// ���̾�
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Wraith);

			// ��Ű��
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Valkyrie);

			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center);

			// ���̾� �۽Ǹ�Ƽ
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Science_Facility);
			// ���̾� ���� - ���
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Irradiate, false);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.EMP_Shockwave, false);
			// ���̾� ���� ������ ��
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Titan_Reactor, false);

			// ���̾� ����
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Science_Vessel);
			// ���̾� �۽Ǹ�Ƽ - ��Ʋũ���� ���� ����
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Physics_Lab);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Yamato_Gun, false);
			// ��Ʋũ���� ������ ��
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Colossus_Reactor, false);
			// ��Ʋũ����
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Battlecruiser);

			// ���̾� �۽Ǹ�Ƽ - ��Ʈ ���� ����
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Science_Facility);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Covert_Ops);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Lockdown, false);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Personnel_Cloaking, false);
			// ��Ʈ ���ðŸ� ��
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Ocular_Implants, false);
			// ��Ʈ ������ ��
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Moebius_Reactor, false);

			// ��Ʈ
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Ghost);

			// ����ź
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Nuclear_Silo);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Nuclear_Missile);
			*/
		} 
		else if (MyBotModule.Broodwar.self().getRace() == Race.Zerg) {
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Spawning_Pool,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getBasicSupplyProviderUnitType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);

			/*
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hatchery,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hatchery,
					BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hatchery,
					BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(
					InformationManager.Instance().getBasicSupplyProviderUnitType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);

			// ���� �ͽ�Ʈ����
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Extractor,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation);

			// ��ū �ݷδ�
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Creep_Colony,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Sunken_Colony,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation);

			BuildManager.Instance().buildQueue
					.queueAsLowestPriority(InformationManager.Instance().getRefineryBuildingType());

			// ���۸� �̵��ӵ� ��
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Metabolic_Boost);

			// ������� è��
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Evolution_Chamber);
			// ������� è�� . �������� ���׷��̵�
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Zerg_Melee_Attacks, false);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Zerg_Missile_Attacks, false);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Zerg_Carapace, false);

			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType());
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType());
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType());
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType());
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType());
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType());
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType());
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType());
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType());
			
			// ������ �ڷδ�
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Creep_Colony,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Spore_Colony,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation);

			// �����
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk_Den);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk);

			// ����
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Lair);

			// �����ε� ��ݰ���
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Ventral_Sacs);
			// �����ε� �þ� ����
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Antennae);
			// �����ε� �ӵ� ����
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Pneumatized_Carapace);

			// ����� �̵��ӵ� ��
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Muscular_Augments, false);
			// ����� ���� �����Ÿ� ��
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Grooved_Spines, false);

			// ��Ŀ
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Lurker_Aspect);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Lurker);

			// �����̾�
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Spire, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Mutalisk, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Scourge, true);

			// �����̾� . �������� ���׷��̵�
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Zerg_Flyer_Attacks, false);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Zerg_Flyer_Carapace, false);

			// ��
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Queens_Nest);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Queen);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Spawn_Broodlings, false);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Ensnare, false);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Gamete_Meiosis, false);

			// ���̺�
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hive);
			// ���۸� ���� �ӵ� ��
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Adrenal_Glands, false);

			// �����̾� . �׷���Ʈ �����̾�
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Greater_Spire, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Mutalisk, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Guardian, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Mutalisk, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Devourer, true);

			// ��Ʈ�󸮽�ũ
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Ultralisk_Cavern);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Ultralisk);
			// ��Ʈ�󸮽�ũ �̵��ӵ� ��
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Anabolic_Synthesis, false);
			// ��Ʈ�󸮽�ũ ���� ��
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Chitinous_Plating, false);

			// �����Ϸ�
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Defiler_Mound);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Defiler);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Consume, false);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Plague, false);
			// �����Ϸ� ������ ��
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Metasynaptic_Node, false);

			// ���̴��� ĳ��
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Nydus_Canal);

			// �����, Zerg_Nydus_Canal �ǹ��κ��� Nydus Canal Exit�� ����� ����� ������ �����ϴ�
			//if (MyBotModule.Broodwar.self().completedUnitCount(UnitType.Zerg_Nydus_Canal) > 0) {
			//	for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
			//		if (unit.getType() == UnitType.Zerg_Nydus_Canal) {
			//			TilePosition targetTilePosition = new TilePosition(unit.getTilePosition().getX() + 6, unit.getTilePosition().getY()); // Creep �� �ִ� ���̾�� �Ѵ�
			//			unit.build(UnitType.Zerg_Nydus_Canal, targetTilePosition);
			//		}
			//	}
			//}

			// �� - ���佺Ƽ�� �׶� : �׶� Terran_Command_Center �ǹ��� HitPoint�� ���� ��, ���� �鿩������ Zerg_Infested_Command_Center �� �ٲٸ�, �� �ǹ����� ���� ��
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Infested_Terran);
			*/
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

			// ���� ������ 2�� �̻� ����Ǿ���, ���� ��ġ�� �ľǵǾ����� �Ѱ��� ���� ��ȯ
			if (MyBotModule.Broodwar.self().completedUnitCount(InformationManager.Instance().getBasicCombatUnitType()) > 2) {
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