#include "StrategyManager.h"

using namespace MyBot;

StrategyManager & StrategyManager::Instance()
{
	static StrategyManager instance;
	return instance;
}

StrategyManager::StrategyManager()
{
	isFullScaleAttackStarted = false;
	isInitialBuildOrderFinished = false;
}

void StrategyManager::onStart()
{
	setInitialBuildOrder();
}

void StrategyManager::onEnd(bool isWinner)
{	
}

void StrategyManager::update()
{
	if (BuildManager::Instance().buildQueue.isEmpty()) {
		isInitialBuildOrderFinished = true;
	}
		
	executeWorkerTraining();

	executeSupplyManagement();

	executeBasicCombatUnitTraining();

	executeCombat();
}

void StrategyManager::setInitialBuildOrder()
{
	if (BWAPI::Broodwar->self()->getRace() == BWAPI::Races::Protoss) {

		BuildManager::Instance().buildQueue.queueAsLowestPriority(InformationManager::Instance().getWorkerType());
		BuildManager::Instance().buildQueue.queueAsLowestPriority(InformationManager::Instance().getWorkerType());
		BuildManager::Instance().buildQueue.queueAsLowestPriority(InformationManager::Instance().getBasicSupplyProviderUnitType(), BuildOrderItem::SeedPositionStrategy::MainBaseLocation);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(InformationManager::Instance().getWorkerType());
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Protoss_Gateway, BuildOrderItem::SeedPositionStrategy::MainBaseLocation);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(InformationManager::Instance().getWorkerType());
		BuildManager::Instance().buildQueue.queueAsLowestPriority(InformationManager::Instance().getWorkerType());
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Protoss_Zealot);

		/*
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Protoss_Assimilator, BuildOrderItem::SeedPositionStrategy::MainBaseLocation);

		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Protoss_Forge, BuildOrderItem::SeedPositionStrategy::MainBaseLocation);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Protoss_Photon_Cannon, BuildOrderItem::SeedPositionStrategy::MainBaseLocation);

		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Protoss_Gateway, BuildOrderItem::SeedPositionStrategy::MainBaseLocation);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Protoss_Zealot);

		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Protoss_Cybernetics_Core, BuildOrderItem::SeedPositionStrategy::MainBaseLocation);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Protoss_Dragoon);
		// ��� �����Ÿ� ���׷��̵�
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Singularity_Charge);

		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Protoss_Citadel_of_Adun);
		// ���� �ӵ� ���׷��̵�
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Leg_Enhancements);

		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Protoss_Shield_Battery);

		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Protoss_Templar_Archives);
		// �������÷�
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Protoss_High_Templar);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Protoss_High_Templar);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::TechTypes::Psionic_Storm);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::TechTypes::Hallucination);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Khaydarin_Amulet);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Protoss_Archon);

		// ��ũ��ĭ
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Protoss_Dark_Templar);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Protoss_Dark_Templar);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::TechTypes::Maelstrom);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::TechTypes::Mind_Control);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Argus_Talisman);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Protoss_Dark_Archon);

		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Protoss_Robotics_Facility);

		// ��Ʋ
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Protoss_Shuttle);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Protoss_Robotics_Support_Bay);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Gravitic_Drive);

		// ����
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Protoss_Reaver);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Scarab_Damage);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Reaver_Capacity);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Protoss_Scarab);

		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Protoss_Observatory);
		// ������
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Protoss_Observer);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Gravitic_Boosters);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Sensor_Array);

		// ��������
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Protoss_Stargate);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Protoss_Fleet_Beacon);

		// ��ī��Ʈ
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Protoss_Scout);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Apial_Sensors);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Gravitic_Thrusters);

		// Ŀ����
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Protoss_Corsair);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::TechTypes::Disruption_Web);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Argus_Jewel);

		// ĳ����
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Protoss_Carrier);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Carrier_Capacity);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Protoss_Interceptor);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Protoss_Interceptor);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Protoss_Interceptor);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Protoss_Interceptor);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Protoss_Interceptor);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Protoss_Interceptor);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Protoss_Interceptor);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Protoss_Interceptor);

		// �ƺ���
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Protoss_Arbiter_Tribunal);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Protoss_Arbiter);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::TechTypes::Recall);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::TechTypes::Stasis_Field);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Khaydarin_Core);

		// ���� - ���� ���� ���׷��̵�
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Protoss_Ground_Weapons);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Protoss_Plasma_Shields);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Protoss_Ground_Armor);

		// ���̹���ƽ���ھ� - ���� ���� ���׷��̵�
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Protoss_Air_Weapons);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Protoss_Air_Armor);

		*/
	}
	else if (BWAPI::Broodwar->self()->getRace() == BWAPI::Races::Terran) {
		BuildManager::Instance().buildQueue.queueAsLowestPriority(InformationManager::Instance().getWorkerType());
		BuildManager::Instance().buildQueue.queueAsLowestPriority(InformationManager::Instance().getWorkerType());
		BuildManager::Instance().buildQueue.queueAsLowestPriority(InformationManager::Instance().getBasicSupplyProviderUnitType(), BuildOrderItem::SeedPositionStrategy::MainBaseLocation);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(InformationManager::Instance().getWorkerType());
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Terran_Barracks, BuildOrderItem::SeedPositionStrategy::MainBaseLocation);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(InformationManager::Instance().getWorkerType());
		BuildManager::Instance().buildQueue.queueAsLowestPriority(InformationManager::Instance().getWorkerType());
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Terran_Marine);

		/*
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Terran_Barracks, BuildOrderItem::SeedPositionStrategy::MainBaseLocation);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Terran_Bunker, BuildOrderItem::SeedPositionStrategy::MainBaseLocation);
		
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Terran_Academy);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Terran_Comsat_Station);

		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Terran_Marine);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Terran_Firebat);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Terran_Medic);

		// ���� �����̳ʸ�
		BuildManager::Instance().buildQueue.queueAsLowestPriority(InformationManager::Instance().getRefineryBuildingType());
		
		// �������� ���׷��̵�
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Terran_Engineering_Bay);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Terran_Infantry_Weapons);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Terran_Infantry_Armor);

		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Terran_Missile_Turret);

		// ���� ������
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::TechTypes::Stim_Packs);
		// ���� �����Ÿ� ��
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::U_238_Shells);

		// �޵�
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::TechTypes::Optical_Flare);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::TechTypes::Restoration);
		// �޵� ������ ��
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Caduceus_Reactor);

		// ���丮
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Terran_Factory);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Terran_Machine_Shop);
		// ���� �����̴� ����
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::TechTypes::Spider_Mines);
		// ���� �̵��ӵ� ��
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Ion_Thrusters);
		// ������ũ ������
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Terran_Siege_Tank_Tank_Mode);

		// ����
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Terran_Vulture);

		// ������ũ
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::TechTypes::Tank_Siege_Mode);

		// �ƸӴ�
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Terran_Armory);
		// ���� ��ī�� ���� ���׷��̵�
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Terran_Vehicle_Plating);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Terran_Vehicle_Weapons);
		// ���� ���� ���׷��̵�
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Terran_Ship_Plating);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Terran_Ship_Weapons);
		// �񸮾� �����Ÿ� ��
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Charon_Boosters);

		// �񸮾�
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Terran_Goliath);

		// ��Ÿ��Ʈ
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Terran_Starport);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Terran_Control_Tower);
		// ���̾� Ŭ��ŷ
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::TechTypes::Cloaking_Field);
		// ���̾� ������ ��
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Apollo_Reactor);

		// ���̾�
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Terran_Wraith);

		// ��Ű��
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Terran_Valkyrie);

		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Terran_Command_Center);

		// ���̾� �۽Ǹ�Ƽ
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Terran_Science_Facility);
		// ���̾� ���� - ���
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::TechTypes::Irradiate);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::TechTypes::EMP_Shockwave);
		// ���̾� ���� ������ ��
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Titan_Reactor);

		// ���̾� ����
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Terran_Science_Vessel);
		// ���̾� �۽Ǹ�Ƽ - ��Ʋũ���� ���� ����
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Terran_Physics_Lab);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::TechTypes::Yamato_Gun);
		// ��Ʋũ���� ������ ��
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Colossus_Reactor);
		// ��Ʋũ����
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Terran_Battlecruiser);

		// ���̾� �۽Ǹ�Ƽ - ��Ʈ ���� ����
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Terran_Science_Facility);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Terran_Covert_Ops);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::TechTypes::Lockdown);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::TechTypes::Personnel_Cloaking);
		// ��Ʈ ���ðŸ� ��
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Ocular_Implants);
		// ��Ʈ ������ ��
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Moebius_Reactor);

		// ��Ʈ
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Terran_Ghost);

		// ����ź
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Terran_Command_Center);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Terran_Nuclear_Silo);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Terran_Nuclear_Missile);
		*/
	}
	else if (BWAPI::Broodwar->self()->getRace() == BWAPI::Races::Zerg) {
		BuildManager::Instance().buildQueue.queueAsLowestPriority(InformationManager::Instance().getWorkerType());
		BuildManager::Instance().buildQueue.queueAsLowestPriority(InformationManager::Instance().getWorkerType());
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Zerg_Spawning_Pool);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(InformationManager::Instance().getWorkerType());
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Zerg_Zergling);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Zerg_Zergling);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Zerg_Zergling);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(InformationManager::Instance().getBasicSupplyProviderUnitType(), BuildOrderItem::SeedPositionStrategy::MainBaseLocation);

		/*		
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Zerg_Hatchery, BuildOrderItem::SeedPositionStrategy::MainBaseLocation);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(InformationManager::Instance().getWorkerType());
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Zerg_Hatchery, BuildOrderItem::SeedPositionStrategy::FirstExpansionLocation);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(InformationManager::Instance().getWorkerType());
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Zerg_Hatchery, BuildOrderItem::SeedPositionStrategy::FirstExpansionLocation);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(InformationManager::Instance().getWorkerType());

		BuildManager::Instance().buildQueue.queueAsLowestPriority(InformationManager::Instance().getBasicSupplyProviderUnitType(), BuildOrderItem::SeedPositionStrategy::MainBaseLocation);

		// ���� �ͽ�Ʈ����
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Zerg_Extractor, BuildOrderItem::SeedPositionStrategy::MainBaseLocation);

		// ��ū �ݷδ�
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Zerg_Creep_Colony, BuildOrderItem::SeedPositionStrategy::MainBaseLocation);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Zerg_Sunken_Colony, BuildOrderItem::SeedPositionStrategy::MainBaseLocation);

		BuildManager::Instance().buildQueue.queueAsLowestPriority(InformationManager::Instance().getRefineryBuildingType());

		// ���۸� �̵��ӵ� ��
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Metabolic_Boost);

		BuildManager::Instance().buildQueue.queueAsLowestPriority(InformationManager::Instance().getWorkerType());
		BuildManager::Instance().buildQueue.queueAsLowestPriority(InformationManager::Instance().getWorkerType());
		BuildManager::Instance().buildQueue.queueAsLowestPriority(InformationManager::Instance().getWorkerType());
		BuildManager::Instance().buildQueue.queueAsLowestPriority(InformationManager::Instance().getWorkerType());
		BuildManager::Instance().buildQueue.queueAsLowestPriority(InformationManager::Instance().getWorkerType());
		BuildManager::Instance().buildQueue.queueAsLowestPriority(InformationManager::Instance().getWorkerType());
		BuildManager::Instance().buildQueue.queueAsLowestPriority(InformationManager::Instance().getWorkerType());
		BuildManager::Instance().buildQueue.queueAsLowestPriority(InformationManager::Instance().getWorkerType());
		BuildManager::Instance().buildQueue.queueAsLowestPriority(InformationManager::Instance().getWorkerType());

		// ������� è��
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Zerg_Evolution_Chamber);
		// ������� è�� -> �������� ���׷��̵�
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Zerg_Melee_Attacks, false);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Zerg_Missile_Attacks, false);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Zerg_Carapace, false);

		// ������ �ڷδ�
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Zerg_Creep_Colony, BuildOrderItem::SeedPositionStrategy::MainBaseLocation);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Zerg_Spore_Colony, BuildOrderItem::SeedPositionStrategy::MainBaseLocation);

		// �����
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Zerg_Hydralisk_Den);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Zerg_Hydralisk);

		// ����
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Zerg_Lair);

		// �����ε� ��ݰ���
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Ventral_Sacs);
		// �����ε� �þ� ����
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Antennae);
		// �����ε� �ӵ� ����
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Pneumatized_Carapace);

		// ����� �̵��ӵ� ��
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Muscular_Augments, false);
		// ����� ���� �����Ÿ� ��
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Grooved_Spines, false);

		// ��Ŀ
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::TechTypes::Lurker_Aspect);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Zerg_Hydralisk);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Zerg_Lurker);

		// �����̾�
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Zerg_Spire);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Zerg_Mutalisk);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Zerg_Scourge);

		// �����̾� -> �������� ���׷��̵�
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Zerg_Flyer_Attacks, false);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Zerg_Flyer_Carapace, false);

		// ��
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Zerg_Queens_Nest);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Zerg_Queen);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::TechTypes::Spawn_Broodlings, false);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::TechTypes::Ensnare, false);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Gamete_Meiosis, false);

		// ���̺�
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Zerg_Hive);
		// ���۸� ���� �ӵ� ��
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Adrenal_Glands, false);

		// �����̾� -> �׷���Ʈ �����̾�
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Zerg_Greater_Spire);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Zerg_Guardian);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Zerg_Devourer);

		// ��Ʈ�󸮽�ũ
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Zerg_Ultralisk_Cavern);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Zerg_Ultralisk);
		// ��Ʈ�󸮽�ũ �̵��ӵ� ��
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Anabolic_Synthesis, false);
		// ��Ʈ�󸮽�ũ ���� ��
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Chitinous_Plating, false);

		// �����Ϸ�
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Zerg_Defiler_Mound);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Zerg_Defiler);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::TechTypes::Consume, false);
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::TechTypes::Plague, false);
		// �����Ϸ� ������ ��
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UpgradeTypes::Metasynaptic_Node, false);


		// ���̴��� ĳ��
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Zerg_Nydus_Canal);
		
		// �����, Zerg_Nydus_Canal �ǹ��κ��� Nydus Canal Exit�� ����� ����� ������ �����ϴ�
		//
		//if (BWAPI::Broodwar->self()->completedUnitCount(BWAPI::UnitTypes::Zerg_Nydus_Canal) > 0) {
		//	for (auto & unit : BWAPI::Broodwar->self()->getUnits()) {
		//		if (unit->getType() == BWAPI::UnitTypes::Zerg_Nydus_Canal) {
		//			BWAPI::TilePosition targetTilePosition(unit->getTilePosition().x + 6, unit->getTilePosition().y); // Creep �� �ִ� ���̾�� �Ѵ�
		//			unit->build(BWAPI::UnitTypes::Zerg_Nydus_Canal, targetTilePosition);
		//		}
		//	}
		//}

		// �� - ���佺Ƽ�� �׶� : �׶� Terran_Command_Center �ǹ��� HitPoint�� ���� ��, ���� �鿩������ Zerg_Infested_Command_Center �� �ٲٸ�, �� �ǹ����� ���� ��
		BuildManager::Instance().buildQueue.queueAsLowestPriority(BWAPI::UnitTypes::Zerg_Infested_Terran);
		*/
	}
}

// �ϲ� ��� �߰� ����
void StrategyManager::executeWorkerTraining()
{
	// InitialBuildOrder �����߿��� �ƹ��͵� ���� �ʽ��ϴ�
	if (isInitialBuildOrderFinished == false) {
		return;
	}

	if (BWAPI::Broodwar->self()->minerals() >= 50) {
		// workerCount = ���� �ϲ� �� + �������� �ϲ� ��
		int workerCount = BWAPI::Broodwar->self()->allUnitCount(InformationManager::Instance().getWorkerType());

		if (BWAPI::Broodwar->self()->getRace() == BWAPI::Races::Zerg) {

			for (auto & unit : BWAPI::Broodwar->self()->getUnits())
			{
				if (unit->getType() == BWAPI::UnitTypes::Zerg_Egg) {
					// Zerg_Egg ���� morph ����� ������ isMorphing = true, isBeingConstructed = true, isConstructing = true �� �ȴ�
					// Zerg_Egg �� �ٸ� �������� �ٲ�鼭 ���� ������� ������ ��� isBeingConstructed = true, isConstructing = true �� �Ǿ��ٰ�, 
					if (unit->isMorphing() && unit->getBuildType() == BWAPI::UnitTypes::Zerg_Drone) {
						workerCount++;
					}
				}
			}
		}
		else {
			for (auto & unit : BWAPI::Broodwar->self()->getUnits())
			{
				if (unit->getType().isResourceDepot())
				{
					if (unit->isTraining()) {
						workerCount += unit->getTrainingQueue().size();
					}
				}
			}
		}

		if (workerCount < 30) {
			for (auto & unit : BWAPI::Broodwar->self()->getUnits())
			{
				if (unit->getType().isResourceDepot())
				{
					if (unit->isTraining() == false || unit->getLarva().size() > 0) {

						// ����ť�� �ϲ� ������ 1���� �ֵ��� �Ѵ�
						if (BuildManager::Instance().buildQueue.getItemCount(InformationManager::Instance().getWorkerType()) == 0) {
							//std::cout << "worker enqueue" << std::endl;
							BuildManager::Instance().buildQueue.queueAsLowestPriority(MetaType(InformationManager::Instance().getWorkerType()), false);
						}
					}
				}
			}
		}
	}
}

// Supply DeadLock ���� �� SupplyProvider �� �������� ��Ȳ �� ���� ������ �������μ� SupplyProvider�� �߰� �Ǽ�/�����Ѵ�
void StrategyManager::executeSupplyManagement()
{
	// InitialBuildOrder �����߿��� �ƹ��͵� ���� �ʽ��ϴ�
	if (isInitialBuildOrderFinished == false) {
		return;
	}

	// 1�ʿ� �ѹ��� ����
	if (BWAPI::Broodwar->getFrameCount() % 24 != 0) {
		return;
	}

	// ���ӿ����� ���ö��� ���� 200���� ������, BWAPI ������ ���ö��� ���� 400���� �ִ�
	// ���۸� 1������ ���ӿ����� ���ö��̸� 0.5 ����������, BWAPI ������ ���ö��̸� 1 �����Ѵ�
	if (BWAPI::Broodwar->self()->supplyTotal() <= 400)
	{
		// ���ö��̰� �� ��á���� �� ���ö��̸� ������ ������ ���� �Ͼ�Ƿ�, supplyMargin (���ӿ����� ���ö��� ���� ���� 2��)��ŭ ���������� �� ���ö��̸� ������ �Ѵ�
		// �̷��� ���� ���س�����, ���� �ʹݺο��� ���ö��̸� �ʹ� ���� ����, ���� �Ĺݺο��� ���ö��̸� �ʹ� �ʰ� ���� �ȴ�
		int supplyMargin = 12;

		// currentSupplyShortage �� ����Ѵ�
		int currentSupplyShortage = BWAPI::Broodwar->self()->supplyUsed() + supplyMargin - BWAPI::Broodwar->self()->supplyTotal();

		if (currentSupplyShortage > 0) {

			// ����/�Ǽ� ���� Supply�� ����
			int onBuildingSupplyCount = 0;

			// ���� ������ ���, �������� Zerg_Overlord (Zerg_Egg) �� ����. Hatchery �� �ǹ��� ���� �ʴ´�
			if (BWAPI::Broodwar->self()->getRace() == BWAPI::Races::Zerg) {
				for (auto & unit : BWAPI::Broodwar->self()->getUnits())
				{
					if (unit->getType() == BWAPI::UnitTypes::Zerg_Egg && unit->getBuildType() == BWAPI::UnitTypes::Zerg_Overlord) {
						onBuildingSupplyCount += BWAPI::UnitTypes::Zerg_Overlord.supplyProvided();
					}
					// ���¾ Overlord �� ���� SupplyTotal �� �ݿ��ȵǾ, �߰� ī��Ʈ�� ������� 
					if (unit->getType() == BWAPI::UnitTypes::Zerg_Overlord && unit->isConstructing()) {
						onBuildingSupplyCount += BWAPI::UnitTypes::Zerg_Overlord.supplyProvided();
					}
				}
			}
			// ���� ������ �ƴ� ���, �Ǽ����� Protoss_Pylon, Terran_Supply_Depot �� ����. Nexus, Command Center �� �ǹ��� ���� �ʴ´�
			else {
				onBuildingSupplyCount += ConstructionManager::Instance().getConstructionQueueItemCount(InformationManager::Instance().getBasicSupplyProviderUnitType()) * InformationManager::Instance().getBasicSupplyProviderUnitType().supplyProvided();
			}

			std::cout << "currentSupplyShortage : " << currentSupplyShortage << " onBuildingSupplyCount : " << onBuildingSupplyCount << std::endl;

			if (currentSupplyShortage > onBuildingSupplyCount) {

				// BuildQueue �ֻ�ܿ� SupplyProvider �� ���� ������ enqueue �Ѵ�
				bool isToEnqueue = true;
				if (!BuildManager::Instance().buildQueue.isEmpty()) {
					BuildOrderItem currentItem = BuildManager::Instance().buildQueue.getHighestPriorityItem();
					if (currentItem.metaType.isUnit() && currentItem.metaType.getUnitType() == InformationManager::Instance().getBasicSupplyProviderUnitType()) {
						isToEnqueue = false;
					}
				}
				if (isToEnqueue) {
					std::cout << "enqueue supply provider " << InformationManager::Instance().getBasicSupplyProviderUnitType().getName().c_str() << std::endl;
					BuildManager::Instance().buildQueue.queueAsHighestPriority(MetaType(InformationManager::Instance().getBasicSupplyProviderUnitType()), true);
				}
			}

		}
	}
}

void StrategyManager::executeBasicCombatUnitTraining()
{
	// InitialBuildOrder �����߿��� �ƹ��͵� ���� �ʽ��ϴ�
	if (isInitialBuildOrderFinished == false) {
		return;
	}

	// �⺻ ���� �߰� �Ʒ�
	if (BWAPI::Broodwar->self()->minerals() >= 200 && BWAPI::Broodwar->self()->supplyUsed() < 390) {
		{
			for (auto & unit : BWAPI::Broodwar->self()->getUnits())
			{
				if (unit->getType() == InformationManager::Instance().getBasicCombatBuildingType()) {
					if (unit->isTraining() == false || unit->getLarva().size() > 0) {
						if (BuildManager::Instance().buildQueue.getItemCount(InformationManager::Instance().getBasicCombatUnitType()) == 0) {
							BuildManager::Instance().buildQueue.queueAsLowestPriority(InformationManager::Instance().getBasicCombatUnitType());
						}
					}
				}
			}
		}
	}
}


void StrategyManager::executeCombat()
{
	// ���� ��尡 �ƴ� ������ �������ֵ��� �Ʊ� ���� ��� ������Ѽ� ���
	if (isFullScaleAttackStarted == false)		
	{
		BWTA::Chokepoint* firstChokePoint = BWTA::getNearestChokepoint(InformationManager::Instance().getMainBaseLocation(InformationManager::Instance().selfPlayer)->getTilePosition());

		for (auto & unit : BWAPI::Broodwar->self()->getUnits())
		{
			if (unit->getType() == InformationManager::Instance().getBasicCombatUnitType() && unit->isIdle()) {
				CommandUtil::attackMove(unit, firstChokePoint->getCenter());
			}
		}

		// ���� ������ 2�� �̻� ����Ǿ���, ���� ��ġ�� �ľǵǾ����� �Ѱ��� ���� ��ȯ
		if (BWAPI::Broodwar->self()->completedUnitCount(InformationManager::Instance().getBasicCombatUnitType()) > 2) {

			if (InformationManager::Instance().enemyPlayer != nullptr
				&& InformationManager::Instance().enemyRace != BWAPI::Races::Unknown
				&& InformationManager::Instance().getOccupiedBaseLocations(InformationManager::Instance().enemyPlayer).size() > 0)
			{				
				isFullScaleAttackStarted = true;
			}
		}

	}
	// ���� ��尡 �Ǹ�, ��� �������ֵ��� ���� Main BaseLocation �� ���� ������ �մϴ�
	else {
		//std::cout << "enemy OccupiedBaseLocations : " << InformationManager::Instance().getOccupiedBaseLocations(InformationManager::Instance().enemyPlayer).size() << std::endl;
		
		if (InformationManager::Instance().enemyPlayer != nullptr
			&& InformationManager::Instance().enemyRace != BWAPI::Races::Unknown
			&& InformationManager::Instance().getOccupiedBaseLocations(InformationManager::Instance().enemyPlayer).size() > 0)
		{
			// ���� ��� ���� ����
			BWTA::BaseLocation * targetBaseLocation = nullptr;
			double closestDistance = 100000000;

			for (BWTA::BaseLocation * baseLocation : InformationManager::Instance().getOccupiedBaseLocations(InformationManager::Instance().enemyPlayer)) {

				double distance = BWTA::getGroundDistance(
					InformationManager::Instance().getMainBaseLocation(InformationManager::Instance().selfPlayer)->getTilePosition(), 
					baseLocation->getTilePosition());

				if (distance < closestDistance) {
					closestDistance = distance;
					targetBaseLocation = baseLocation;
				}
			}

			if (targetBaseLocation != nullptr) {

				for (auto & unit : BWAPI::Broodwar->self()->getUnits())
				{
					// �ǹ��� ����
					if (unit->getType().isBuilding()) continue;
					// ��� �ϲ��� ����
					if (unit->getType().isWorker()) continue;

					// canAttack ������ attackMove Command �� ������ �����ϴ�
					if (unit->canAttack()) {

						if (unit->isIdle()) {
							CommandUtil::attackMove(unit, targetBaseLocation->getPosition());
						}
					}
				}
			}
		}
	}
}

