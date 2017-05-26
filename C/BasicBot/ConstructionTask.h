#pragma once

#include "Common.h"

namespace MyBot
{
	/// �ǹ� �Ǽ� Construction Task �� ���� ����
	namespace ConstructionStatus
	{
		enum { 
			Unassigned = 0,				///< Construction �ϲ��� ������ �Ǿ��ִ� ����
			Assigned = 1,				///< Construction �ϲ��� ���� �Ǿ�����, Construction �ϲ��� �Ǽ��� ���������� ���� ����
			UnderConstruction = 2		///< Construction �ϲ��� ���� �Ǿ� �Ǽ� �۾��� �ϰ��ִ� ����
		};
	}

	/// �ǹ� �Ǽ� Construction Task �ڷᱸ��
	class ConstructionTask 
	{     
	public:
    
		/// �ǹ��� Ÿ��
		BWAPI::UnitType         type;

		/// �ǹ��� �������� ��ȹ�� ��ġ
		/// �ϲ��� �ǹ��� ������ ���� ���� �ش� ��ġ�� ��ֹ��� �ְ� �Ǵµ� ������ ����� �� ��ġ�� �߽����� �ٽ� �ǹ� ���� ��ġ�� Ž���ؼ� ���մϴ�
		BWAPI::TilePosition     desiredPosition;

		/// �ǹ��� ������ �Ǽ��Ǵ� ��ġ. 
		BWAPI::TilePosition     finalPosition;
		
		/// �ǹ� �Ǽ� Construction Task �� ���� ����
		size_t                  status;

		/// �ش� �ǹ��� �Ǽ� Construction Task �� ���� �ϲ� ����
		BWAPI::Unit             constructionWorker;
		
		/// �ش� �ǹ��� �Ǽ� Construction �� �����ϴ� ����
		/// buildingUnit ���� ó���� nullptr �� ���õǰ�, construction �� ���۵Ǿ� isBeingConstructed, underConstrunction ���°� �Ǿ�� ��μ� ���� ä������
		BWAPI::Unit             buildingUnit;

		/// �ش� �ǹ��� �Ǽ� Construction Task �� ���� �ϲ� ���ֿ��� build ����� �����Ͽ����� ����.
		/// �ѹ��� �Ȱ��� Ÿ�Ͽ��� build ����� ���� �� �����Ƿ�, �ϴ� buildCommandGiven = false �� ���·� �ϲ��� �ش� Ÿ�� ��ġ�� �̵���Ų ��, 
		/// �ϲ��� �ش� Ÿ�� ��ġ ��ó�� ���� buildCommand ���ø� �մϴ�
		bool                    buildCommandGiven;

		/// �ش� �ǹ��� �Ǽ� Construction Task �� ���� �ϲ� ���ֿ��� build ����� ������ �ð�
		int                     lastBuildCommandGivenFrame;

		/// �ش� �ǹ��� �Ǽ� Construction Task �� �ֱٿ� �޾Ҵ� �ϲ� ������ ID
		/// �ϲ� ������ Construction Task �� �޾����� ���� ������ ���ϴ� ������ ���, ���Ӱ� �ϲ� ������ �����ؼ� Construction Task �� �ο��ϴµ�, 
		/// �Ź� �Ȱ��� �ϲ� ������ Construction Task �� ���� �ʰ� �ϱ� ���ؼ� ����
		int                     lastConstructionWorkerID;

		/// Construction Task �� �Ǽ� �۾� �����ߴ°� ����
		bool                    underConstruction;

		ConstructionTask() 
			: desiredPosition   (BWAPI::TilePositions::None)
			, finalPosition     (BWAPI::TilePositions::None)
			, type              (BWAPI::UnitTypes::Unknown)
			, buildingUnit      (nullptr)
			, constructionWorker       (nullptr)
			, lastBuildCommandGivenFrame(0)
			, lastConstructionWorkerID(0)
			, status            (ConstructionStatus::Unassigned)
			, buildCommandGiven (false)
			, underConstruction (false) 
		{} 

		ConstructionTask(BWAPI::UnitType t, BWAPI::TilePosition _desiredPosition)
			: desiredPosition   (_desiredPosition)
			, finalPosition     (BWAPI::TilePositions::None)
			, type              (t)
			, buildingUnit      (nullptr)
			, constructionWorker       (nullptr)
			, lastBuildCommandGivenFrame(0)
			, lastConstructionWorkerID(0)
			, status            (ConstructionStatus::Unassigned)
			, buildCommandGiven (false)
			, underConstruction (false) 
		{}

		// equals operator
		bool operator==(const ConstructionTask & b) 
		{
			if (b.type == this->type 
				&& b.desiredPosition.x == this->desiredPosition.x && b.desiredPosition.y == this->desiredPosition.y) {
				// buildings are equal if their worker unit or building unit are equal
				return (b.buildingUnit == buildingUnit) || (b.constructionWorker == constructionWorker);
			}
			else {
				return false;
			}
		}
	};
}