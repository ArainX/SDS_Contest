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
	/// �� ���α׷� ������ ���Ǽ� ����� ���� ���� ȭ�鿡 �߰� �������� ǥ���ϴ� class
	/// ���� Manager ��κ��� ������ ��ȸ�Ͽ� Screen Ȥ�� Map �� ������ ǥ���մϴ�
	class UXManager
	{
		UXManager();

		const int dotRadius = 2;

		// ���� ���� ������ Screen �� ǥ���մϴ�
		void drawGameInformationOnScreen(int x, int y);
		
		/// APM (Action Per Minute) ���ڸ� Screen �� ǥ���մϴ�
		void drawAPM(int x, int y);

		/// Players ������ Screen �� ǥ���մϴ�
		void drawPlayers();

		/// Player ���� �� (Force) ���� ������ Screen �� ǥ���մϴ�
		void drawForces();



		/// BWTA ���̺귯���� ���� Map �м� ��� ������ Map �� ǥ���մϴ�
		void drawBWTAResultOnMap();

		/// Tile Position �׸��带 Map �� ǥ���մϴ�
		void drawMapGrid();


		/// BuildOrderQueue �� Screen �� ǥ���մϴ�
		void drawBuildOrderQueueOnScreen(int x, int y);

		/// ConstructionQueue �� Screen �� ǥ���մϴ�
		void drawConstructionQueueOnScreenAndMap(int x, int y);

		/// Construction �� �ϱ� ���� �����ص� Tile ���� Map �� ǥ���մϴ�
		void drawReservedBuildingTilesOnMap();

		/// Construction �� ���� ���ϴ� Tile ���� Map �� ǥ���մϴ�
		void drawTilesToAvoidOnMap();

		/// Build ���� ���¸� Screen �� ǥ���մϴ�
		void drawBuildStatusOnScreen(int x, int y);


		/// Unit �� HitPoint �� �߰� ������ Map �� ǥ���մϴ�
		void drawUnitExtendedInformationOnMap();

		/// UnitType �� ��� ������ Screen �� ǥ���մϴ�
		void drawUnitStatisticsOnScreen(int x, int y);

		/// Unit �� Id �� Map �� ǥ���մϴ�
		void drawUnitIdOnMap();

		/// Unit �� Target ���� �մ� ���� Map �� ǥ���մϴ�
		void drawUnitTargetOnMap();

		/// Bullet �� Map �� ǥ���մϴ� 
		/// Cloaking Unit �� Bullet ǥ�ÿ� ���Դϴ�
		void drawBulletsOnMap();



		/// Worker Unit �� �ڿ�ä�� ��Ȳ�� Map �� ǥ���մϴ�
		void drawWorkerMiningStatusOnMap();

		/// ResourceDepot �� Worker ���ڸ� Map �� ǥ���մϴ�
		void drawWorkerCountOnMap();

		/// Worker Unit ���� ���¸� Screen �� ǥ���մϴ�
		void drawWorkerStateOnScreen(int x, int y);


		/// ���� ���¸� Screen �� ǥ���մϴ�
		void drawScoutInformation(int x, int y);

	public:
		/// static singleton ��ü�� �����մϴ�
		static UXManager &	Instance();

		/// ��Ⱑ ���۵� �� ��ȸ������ �߰� ������ ����մϴ�
		void onStart();

		/// ��� ���� �� �� �����Ӹ��� �߰� ������ ����ϰ� ����� �Է��� ó���մϴ�
		void update();
	};

	/// ���� �����Ȳ�� ���� ���� ������� �����Ͽ� ǥ���ϱ� ���� Comparator class
	class CompareWhenStarted
	{
	public:

		CompareWhenStarted() {}

		/// ���� �����Ȳ�� ���� ���� ������� �����Ͽ� ǥ���ϱ� ���� sorting operator
		bool operator() (BWAPI::Unit u1, BWAPI::Unit u2)
		{
			int startedU1 = BWAPI::Broodwar->getFrameCount() - (u1->getType().buildTime() - u1->getRemainingBuildTime());
			int startedU2 = BWAPI::Broodwar->getFrameCount() - (u2->getType().buildTime() - u2->getRemainingBuildTime());
			return startedU1 > startedU2;
		}
	};

}
