#pragma once

#include "Common.h"
#include "InformationManager.h"
#include "WorkerManager.h"
#include "BuildManager.h"
#include "ConstructionManager.h"
#include "ScoutManager.h"
#include "StrategyManager.h"
#include "UXManager.h"

namespace MyBot
{
	/// ���� �����α׷��� ��ü�� �Ǵ� class
	/// ��Ÿũ����Ʈ ��� ���� �߻��ϴ� �̺�Ʈ���� �����ϰ� ó���ǵ��� �ش� Manager ��ü���� �̺�Ʈ�� �����ϴ� ������ Controller ������ �մϴ�
	class GameCommander 
	{
		/// ��� Manager �� ������ ����Ű���� �˱����� �÷���
		bool isToFindError;

	public:

		GameCommander();
		~GameCommander();
		
		/// ��Ⱑ ���۵� �� ��ȸ������ �߻��ϴ� �̺�Ʈ�� ó���մϴ�
		void onStart();
		/// ��Ⱑ ����� �� ��ȸ������ �߻��ϴ� �̺�Ʈ�� ó���մϴ�
		void onEnd(bool isWinner);
		/// ��� ���� �� �� �����Ӹ��� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
		void onFrame();

		/// �ؽ�Ʈ�� �Է� �� ���͸� �Ͽ� �ٸ� �÷��̾�鿡�� �ؽ�Ʈ�� �����Ϸ� �� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
		void onSendText(std::string text);
		/// �ٸ� �÷��̾�κ��� �ؽ�Ʈ�� ���޹޾��� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
		void onReceiveText(BWAPI::Player player, std::string text);

		/// ����(�ǹ�/��������/��������)�� Create �� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
		void onUnitCreate(BWAPI::Unit unit);
		///  ����(�ǹ�/��������/��������)�� Destroy �� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
		void onUnitDestroy(BWAPI::Unit unit);

		/// ����(�ǹ�/��������/��������)�� Morph �� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
		/// Zerg ������ ������ �ǹ� �Ǽ��̳� ��������/�������� ���꿡�� ���� ��κ� Morph ���·� ����˴ϴ�
		void onUnitMorph(BWAPI::Unit unit);

		/// ����(�ǹ�/��������/��������)�� �Ҽ� �÷��̾ �ٲ� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
		/// Gas Geyser�� � �÷��̾ Refinery �ǹ��� �Ǽ����� ��, Refinery �ǹ��� �ı��Ǿ��� ��, Protoss ���� Dark Archon �� Mind Control �� ���� �Ҽ� �÷��̾ �ٲ� �� �߻��մϴ�
		void onUnitRenegade(BWAPI::Unit unit);
		/// ����(�ǹ�/��������/��������)�� �ϴ� �� (�ǹ� �Ǽ�, ���׷��̵�, �������� �Ʒ� ��)�� ������ �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
		void onUnitComplete(BWAPI::Unit unit);

		/// ����(�ǹ�/��������/��������)�� Discover �� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
		/// �Ʊ� ������ Create �Ǿ��� �� ��簡, ���� ������ Discover �Ǿ��� �� �߻��մϴ�
		void onUnitDiscover(BWAPI::Unit unit);
		/// ����(�ǹ�/��������/��������)�� Evade �� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
		/// ������ Destroy �� �� �߻��մϴ�
		void onUnitEvade(BWAPI::Unit unit);

		/// ����(�ǹ�/��������/��������)�� Show �� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
		/// �Ʊ� ������ Create �Ǿ��� �� ��簡, ���� ������ Discover �Ǿ��� �� �߻��մϴ�
		void onUnitShow(BWAPI::Unit unit);
		/// ����(�ǹ�/��������/��������)�� Hide �� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
		/// ���̴� ������ Hide �� �� �߻��մϴ�
		void onUnitHide(BWAPI::Unit unit);
	};

}