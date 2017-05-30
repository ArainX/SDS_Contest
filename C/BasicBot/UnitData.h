#pragma once

#include "Common.h"

namespace MyBot
{
	/// �ش� Unit�� ID, UnitType, �Ҽ� Player, HitPoint, lastPosition, completed(�ǹ��� �ϼ��� ������) ���� �����صδ� �ڷᱸ��
	/// ���� ������ ��� �Ʊ� �þ� ���� ���� �ʾ� invisible ���°� �Ǿ��� �� ������ ��ȸ�Ҽ��� �������� �ľ��ߴ� ������ ���ǵǱ� ������ ���� �ڷᱸ���� �ʿ��մϴ�
	struct UnitInfo
	{
		int             unitID;
		int             lastHealth;
		int             lastShields;
		BWAPI::Player   player;
		BWAPI::Unit     unit;
		BWAPI::Position lastPosition;
		BWAPI::UnitType type;
		bool            completed;

		UnitInfo()
			: unitID(0)
			, lastHealth(0)
			, player(nullptr)
			, unit(nullptr)
			, lastPosition(BWAPI::Positions::None)
			, type(BWAPI::UnitTypes::None)
			, completed(false)
		{
		}

		const bool operator == (BWAPI::Unit unit) const
		{
			return unitID == unit->getID();
		}

		const bool operator == (const UnitInfo & rhs) const
		{
			return (unitID == rhs.unitID);
		}

		const bool operator < (const UnitInfo & rhs) const
		{
			return (unitID < rhs.unitID);
		}
	};

	typedef std::map<BWAPI::Unit,UnitInfo> UnitAndUnitInfoMap;

	class UnitData
	{
		/// Unit �� UnitInfo �� Map ���·� �����ϴ� �ڷᱸ�� 
		/// C++ ������ Unit �����͸� Key �� ���������, 
		/// JAVA ������ Unit �ڷᱸ���� equals �޽�� ������ ���۵��ϹǷ� Unit.getID() ���� Key �� �����
		UnitAndUnitInfoMap						unitAndUnitInfoMap;

		const bool isBadUnitInfo(const UnitInfo & ui) const;

		/// UnitType�� �ı�/����� ���� ���� ������
		/// C++ ������ UnitType �� ������ ���� Key �� ���������, 
		/// JAVA ������ UnitType �� ������ ���� �����ϹǷ� Unit.getType() ���� Key �� �����
		std::vector<int>						numDeadUnits;
		/// UnitType�� �Ǽ�/�Ʒ��ߴ� ���� ���� ������
		/// C++ ������ UnitType �� ������ ���� Key �� ���������, 
		/// JAVA ������ UnitType �� ������ ���� �����ϹǷ� Unit.getType() ���� Key �� �����
		std::vector<int>						numCreatedUnits;
		/// UnitType�� �����ϴ� ���� ���� ī��Ʈ. ���� ������ ��� �ĺ��� ���� ���� ī��Ʈ
		/// C++ ������ UnitType �� ������ ���� Key �� ���������, 
		/// JAVA ������ UnitType �� ������ ���� �����ϹǷ� Unit.getType() ���� Key �� �����
		std::vector<int>						numUnits;

		/// ����� ������ �����ϴµ� �ҿ�Ǿ��� Mineral �� ������ (�󸶳� ���ظ� ���Ҵ°� ����ϱ� ������)
		int										mineralsLost;
		/// ����� ������ �����ϴµ� �ҿ�Ǿ��� Gas �� ������ (�󸶳� ���ظ� ���Ҵ°� ����ϱ� ������)
		int										gasLost;

	public:

		UnitData();

		/// ������ ���������� ������Ʈ�մϴ�
		void	updateUnitInfo(BWAPI::Unit unit);

		/// �ı�/����� ������ �ڷᱸ������ �����մϴ�
		void	removeUnit(BWAPI::Unit unit);

		/// �����Ͱ� null �� �Ǿ��ų�, �ı��Ǿ� Resource_Vespene_Geyser�� ���ư� Refinery, �������� �ǹ��� �־��� �ɷ� �����صξ��µ� ������ �ı��Ǿ� ������ �ǹ� (Ư��, �׶��� ��� ��Ÿ�� �Ҹ��� �ǹ�) �����͸� �����մϴ�
		void	removeBadUnits();

		/// ����� ������ �����ϴµ� �ҿ�Ǿ��� Mineral �� ������ (�󸶳� ���ظ� ���Ҵ°� ����ϱ� ������) �� �����մϴ�
		int		getMineralsLost()                           const;

		/// ����� ������ �����ϴµ� �ҿ�Ǿ��� Gas �� ������ (�󸶳� ���ظ� ���Ҵ°� ����ϱ� ������)
		int		getGasLost()                                const;

		/// �ش� UnitType �� �ĺ��� Unit ���ڸ� �����մϴ�
		int		getNumUnits(BWAPI::UnitType t)              const;

		/// �ش� UnitType �� �ĺ��� Unit �ı�/��� �������� �����մϴ�
		int		getNumDeadUnits(BWAPI::UnitType t)          const;

		/// �ش� UnitType �� �ĺ��� Unit �Ǽ�/�Ʒ� �������� �����մϴ�
		int		getNumCreatedUnits(BWAPI::UnitType t)		const;

		const	std::map<BWAPI::Unit, UnitInfo> & getUnitAndUnitInfoMap() const;
	};
}