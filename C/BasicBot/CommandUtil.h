#pragma once

#include "Common.h"
#include "Config.h"

namespace MyBot
{
	struct Rect
	{
		int x, y;
		int height, width;
	};

	/// �̵� (move), ���� (attack), ���� (repair), ��Ŭ�� (rightClick)  �� ���� ��Ʈ�� ����� ���� �� ���� üũ�ؾ��� ���׵��� üũ�� �� ��� �������� �ϴ� ���� �Լ���
	namespace CommandUtil
	{
		/// attacker �� target �� �����ϵ��� ��� �մϴ�
		void attackUnit(BWAPI::Unit attacker, BWAPI::Unit target);
		
		/// attacker �� targetPosition �� ���� ���� ������ ��� �մϴ�
		void attackMove(BWAPI::Unit attacker, const BWAPI::Position & targetPosition);

		/// attacker �� targetPosition �� ���� �̵� ������ ��� �մϴ�
		void move(BWAPI::Unit attacker, const BWAPI::Position & targetPosition);

		/// unit �� target �� ���� � ������ �ϵ��� ��� �մϴ�
		/// �ϲ� ������ Mineral Field ���� : Mineral �ڿ� ä��
		/// �ϲ� ������ Refinery �ǹ����� : Gas �ڿ� ä��
		/// ���� ������ �ٸ� �Ʊ� ���ֿ��� : Move ���
		/// ���� ������ �ٸ� ���� ���ֿ��� : Attack ���
		void rightClick(BWAPI::Unit unit, BWAPI::Unit target);

		/// unit �� target �� ���� ���� �ϵ��� ��� �մϴ� 
		void repair(BWAPI::Unit unit, BWAPI::Unit target);
	};

	namespace UnitUtil
	{
		bool IsCombatUnit(BWAPI::Unit unit);
		bool IsValidUnit(BWAPI::Unit unit);
		bool CanAttackAir(BWAPI::Unit unit);
		bool CanAttackGround(BWAPI::Unit unit);
		bool IsGroundTarget(BWAPI::Unit unit);
		bool IsAirTarget(BWAPI::Unit unit);
		bool CanAttack(BWAPI::Unit attacker, BWAPI::Unit target);
		bool CanAttack(BWAPI::UnitType attacker, BWAPI::UnitType target);
		double CalculateLTD(BWAPI::Unit attacker, BWAPI::Unit target);
		int GetAttackRange(BWAPI::Unit attacker, BWAPI::Unit target);
		int GetAttackRange(BWAPI::UnitType attacker, BWAPI::UnitType target);
		int GetTransportSize(BWAPI::UnitType type);
		BWAPI::WeaponType GetWeapon(BWAPI::Unit attacker, BWAPI::Unit target);
		BWAPI::WeaponType GetWeapon(BWAPI::UnitType attacker, BWAPI::UnitType target);

		size_t GetAllUnitCount(BWAPI::UnitType type);

		BWAPI::Unit GetClosestUnitTypeToTarget(BWAPI::UnitType type, BWAPI::Position target);
		double GetDistanceBetweenTwoRectangles(Rect & rect1, Rect & rect2);
	};
}