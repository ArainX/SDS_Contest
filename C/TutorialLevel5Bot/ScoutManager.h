#pragma once

#include "Common.h"
#include "InformationManager.h"

namespace MyBot
{
	namespace ScoutStatus
	{
		enum { 
			NoScout=0,						///< ���� ������ �������� ����
			MovingToAnotherBaseLocation=1,	///< ������ BaseLocation �� �̹߰ߵ� ���¿��� ���� ������ �̵���Ű�� �ִ� ����
			MoveAroundEnemyBaseLocation=2   ///< ������ BaseLocation �� �߰ߵ� ���¿��� ���� ������ �̵���Ű�� �ִ� ����
		};
	}

	/// ���� �ʹݿ� �ϲ� ���� �߿��� ���� ������ �ϳ� �����ϰ�, ���� ������ �̵����� ������ �����ϴ� class
	/// ������ BaseLocation ��ġ�� �˾Ƴ��� �ͱ����� ���ߵǾ��ֽ��ϴ�
	class ScoutManager
	{
		ScoutManager();

		int								currentScoutStatus;

		BWAPI::Unit						currentScoutUnit;

		BWTA::BaseLocation *			currentScoutTargetBaseLocation;
		int								currentScoutTargetDistance;
		
		int                             currentScoutFreeToVertexIndex;		
		std::vector<BWAPI::Position>    enemyBaseRegionVertices;
		BWAPI::Position					currentScoutTargetPosition;
		
		/// ���� ������ �ʿ��ϸ� ���� �����մϴ�
		void							assignScoutIfNeeded();

		/// ���� ������ �̵���ŵ�ϴ�
		void                            moveScoutUnit();

		void                            calculateEnemyRegionVertices();
		BWAPI::Position                 getScoutFleePositionFromEnemyRegionVertices();
		int                             getClosestVertexIndex(BWAPI::Unit unit);

	public:
		/// static singleton ��ü�� �����մϴ�
		static ScoutManager & Instance();

		/// ���� ������ �����ϰ�, ���� ���¸� ������Ʈ�ϰ�, ���� ������ �̵���ŵ�ϴ�
		void update();

		/// ���� ������ �����մϴ�
		BWAPI::Unit getScoutUnit();

		// ���� ���¸� �����մϴ�
		int getScoutStatus();
		
		/// ���� ������ �̵� ��ǥ BaseLocation �� �����մϴ�
		BWTA::BaseLocation * getScoutTargetBaseLocation();
				
		/// ������ Main Base Location �� �ִ� Region �� ��輱�� �ش��ϴ� Vertex ���� ����� �����մϴ�
		std::vector<BWAPI::Position> & getEnemyRegionVertices();
	};
}