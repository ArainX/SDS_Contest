#pragma once

#include "BWAPI.h"
#include <cassert>

/// �� ���α׷� ����
namespace Config
{
	/// �� �⺻ ����
	namespace BotInfo
	{
		/// �� �̸�
		extern std::string BotName;
		/// �� ������ �̸�
		extern std::string BotAuthors;
	}

	/// ���� ���� ����
	namespace Files
    {
		/// �α� ���� �̸�
		extern std::string LogFilename;
		/// �б� ���� ���
		extern std::string ReadDirectory;
		/// ���� ���� ���
		extern std::string WriteDirectory;
    }
	    
	/// CommonUtil ���� ����
	namespace Tools
	{
		/// MapGrid ���� �� �� GridCell �� size
		extern int MAP_GRID_SIZE;
	}
	
	/// BWAPI �ɼ� ���� ����
	namespace BWAPIOptions
    {
		/// ���ÿ��� ������ ������ �� ���ӽ��ǵ� (�ڵ� ���� �� �������� ������ ������ ���� ���� ������ �����)
		/// Speedups for automated play, sets the number of milliseconds bwapi spends in each frame
		/// Fastest: 42 ms/frame.  1�ʿ� 24 frame. �Ϲ������� 1�ʿ� 24frame�� ���� ���Ӽӵ��� �մϴ�
		/// Normal: 67 ms/frame. 1�ʿ� 15 frame
		/// As fast as possible : 0 ms/frame. CPU�� �Ҽ��ִ� ���� ���� �ӵ�. 
        extern int SetLocalSpeed;
		/// ���ÿ��� ������ ������ �� FrameSkip (�ڵ� ���� �� �������� ������ ������ ���� ���� ������ �����)
		/// frameskip�� �ø��� ȭ�� ǥ�õ� ������Ʈ ���ϹǷ� �ξ� �����ϴ�
        extern int SetFrameSkip;
		/// ���ÿ��� ������ ������ �� ����� Ű����/���콺 �Է� ��� ���� (�ڵ� ���� �� �������� ������ ������ ���� ���� ������ �����)
		extern bool EnableUserInput;
		/// ���ÿ��� ������ ������ �� ��ü ������ �� ���̰� �� ������ ���� (�ڵ� ���� �� �������� ������ ������ ���� ���� ������ �����)
        extern bool EnableCompleteMapInformation;
    }
	
	/// ����� ���� ����
	namespace Debug
	{
		/// ȭ�� ǥ�� ���� - ���� ����
		extern bool DrawGameInfo;

		/// ȭ�� ǥ�� ���� - �̳׶�, ����
		extern bool DrawResourceInfo;
		/// ȭ�� ǥ�� ���� - ����
		extern bool DrawBWTAInfo;
		/// ȭ�� ǥ�� ���� - �ٵ���
		extern bool DrawMapGrid;

		/// ȭ�� ǥ�� ���� - ���� HitPoint
		extern bool DrawUnitHealthBars;
		/// ȭ�� ǥ�� ���� - ���� ���
		extern bool DrawEnemyUnitInfo;
		/// ȭ�� ǥ�� ���� - ���� ~ Target �� ����
		extern bool DrawUnitTargetInfo;

		/// ȭ�� ǥ�� ���� - ���� ť
		extern bool DrawProductionInfo;

		/// ȭ�� ǥ�� ���� - �ǹ� Construction ��Ȳ
		extern bool DrawBuildingInfo;

		/// ȭ�� ǥ�� ���� - �ǹ� ConstructionPlace ���� ��Ȳ
		extern bool DrawReservedBuildingTiles;

		/// ȭ�� ǥ�� ���� - ���� ����
		extern bool DrawScoutInfo;
		/// ȭ�� ǥ�� ���� - �ϲ� ���
		extern bool DrawWorkerInfo;

		/// ȭ�� ǥ�� ���� - ���콺 Ŀ��
		extern bool DrawMouseCursorInfo;
	}

	/// ���ӷ��� ���� �Ķ����
	namespace Macro
	{
		/// ������ Refinery ���� ������ �ϲ� �ִ� ����
		extern int WorkersPerRefinery;

		/// �ǹ��� �ǹ��� ��� �ּ����� ���� - �Ϲ����� �ǹ��� ���
		extern int BuildingSpacing;
		/// �ǹ��� �ǹ��� ��� �ּ����� ���� - ResourceDepot �ǹ��� ��� (Nexus, Hatchery, Command Center)
		extern int BuildingResourceDepotSpacing; 
		/// �ǹ��� �ǹ��� ��� �ּ����� ���� - Protoss_Pylon �ǹ��� ��� - ���� �ʱ⿡
		extern int BuildingPylonEarlyStageSpacing;
		/// �ǹ��� �ǹ��� ��� �ּ����� ���� - Protoss_Pylon �ǹ��� ��� - ���� �ʱ� ���Ŀ�
		extern int BuildingPylonSpacing;
		/// �ǹ��� �ǹ��� ��� �ּ����� ���� - Terran_Supply_Depot �ǹ��� ���
		extern int BuildingSupplyDepotSpacing;
		/// �ǹ��� �ǹ��� ��� �ּ����� ���� - ��� �ǹ��� ��� (����ĳ��. ��ū�ݷδ�. �������ݷδ�. �ͷ�. ��Ŀ)
		extern int BuildingDefenseTowerSpacing; 
	}
}