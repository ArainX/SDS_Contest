#pragma once

#include "Common.h"

#include "UnitData.h"
#include "BuildOrderQueue.h"
#include "InformationManager.h"
#include "WorkerManager.h"
#include "BuildManager.h"
#include "ConstructionManager.h"
#include "ScoutManager.h"
#include "StrategyManager.h"

namespace MyBot
{
	/// ��Ȳ�� �Ǵ��Ͽ�, ����, ����, ����, ��� ���� �����ϵ��� �Ѱ� ���ָ� �ϴ� class
	/// InformationManager �� �ִ� ������κ��� ��Ȳ�� �Ǵ��ϰ�, 
	/// BuildManager �� buildQueue�� ���� (�ǹ� �Ǽ� / ���� �Ʒ� / ��ũ ����ġ / ���׷��̵�) ����� �Է��մϴ�.
	/// ����, ����, ����, ��� ���� �����ϴ� �ڵ尡 ���� class
	class StrategyManager
	{
		StrategyManager();

		bool isInitialBuildOrderFinished;
		void setInitialBuildOrder();

		void executeWorkerTraining();
		void executeSupplyManagement();
		void executeBasicCombatUnitTraining();

		bool isFullScaleAttackStarted;
		void executeCombat();

	public:
		/// static singleton ��ü�� �����մϴ�
		static StrategyManager &	Instance();

		/// ��Ⱑ ���۵� �� ��ȸ������ ���� �ʱ� ���� ���� ������ �����մϴ�
		void onStart();

		///  ��Ⱑ ����� �� ��ȸ������ ���� ��� ���� ���� ������ �����մϴ�
		void onEnd(bool isWinner);

		/// ��� ���� �� �� �����Ӹ��� ��� ���� ���� ������ �����մϴ�
		void update();
	};
}
