#include "GameCommander.h"

using namespace MyBot;

GameCommander::GameCommander(){
}
GameCommander::~GameCommander(){
}

void GameCommander::onStart() 
{
	BWAPI::TilePosition startLocation = BWAPI::Broodwar->self()->getStartLocation();
	if (startLocation == BWAPI::TilePositions::None || startLocation == BWAPI::TilePositions::Unknown) {
		return;
	}

	StrategyManager::Instance().onStart();
}

void GameCommander::onEnd(bool isWinner)
{
	StrategyManager::Instance().onEnd(isWinner);
}

void GameCommander::onFrame()
{
	if (BWAPI::Broodwar->isPaused()) {
		return;
	}

	if (Config::Debug::LogToConsole) std::cout << "a";

	// �Ʊ� ���̽� ��ġ. ���� ���̽� ��ġ. �� ���ֵ��� �������� ���� Map �ڷᱸ���� ����/������Ʈ
	InformationManager::Instance().update();

	if (Config::Debug::LogToConsole) std::cout << "b";

	// �� ������ ��ġ�� ��ü MapGrid �ڷᱸ���� ����
	MapGrid::Instance().update();

	if (Config::Debug::LogToConsole) std::cout << "c";

	// economy and base managers
	// �ϲ� ���ֿ� ���� ��� (�ڿ� ä��, �̵� ����) ���� �� ����
	WorkerManager::Instance().update();

	if (Config::Debug::LogToConsole) std::cout << "d";

	// �������ť�� �����ϸ�, ��������� ���� ���� ����(���� �Ʒ�, ��ũ ���׷��̵� ��)�� �����Ѵ�.
	BuildManager::Instance().update();

	if (Config::Debug::LogToConsole) std::cout << "e";

	// ������� �� �ǹ� ���忡 ���ؼ���, �ϲ����� ����, ��ġ����, �Ǽ� �ǽ�, �ߴܵ� �ǹ� ���� �簳�� �����Ѵ�
	ConstructionManager::Instance().update();

	if (Config::Debug::LogToConsole) std::cout << "f";

	// ���� �ʱ� ���� ���� ���� �� ���� ���� ��Ʈ���� �����Ѵ�
	ScoutManager::Instance().update();

	if (Config::Debug::LogToConsole) std::cout << "g";

	// ������ �Ǵ� �� ���� ��Ʈ��
	StrategyManager::Instance().update();

	if (Config::Debug::LogToConsole) std::cout << "h";

	// ȭ�� ��� �� ����� �Է� ó��
	UXManager::Instance().update();

	if (Config::Debug::LogToConsole) std::cout << "i";
}

void GameCommander::onUnitShow(BWAPI::Unit unit)			
{ 
	InformationManager::Instance().onUnitShow(unit); 

	// ResourceDepot �� Worker �� ���� ó��
	WorkerManager::Instance().onUnitShow(unit);
}

void GameCommander::onUnitHide(BWAPI::Unit unit)			
{
	InformationManager::Instance().onUnitHide(unit); 
}

void GameCommander::onUnitCreate(BWAPI::Unit unit)		
{ 
	InformationManager::Instance().onUnitCreate(unit);
}

void GameCommander::onUnitComplete(BWAPI::Unit unit)
{
	InformationManager::Instance().onUnitComplete(unit);
}

void GameCommander::onUnitDestroy(BWAPI::Unit unit)		
{
	// ResourceDepot �� Worker �� ���� ó��
	WorkerManager::Instance().onUnitDestroy(unit);

	InformationManager::Instance().onUnitDestroy(unit); 
}

void GameCommander::onUnitRenegade(BWAPI::Unit unit)
{
	// Vespene_Geyser (���� ����) �� �������� �Ǽ��� ���� ���
	//BWAPI::Broodwar->sendText("A %s [%p] has renegaded. It is now owned by %s", unit->getType().c_str(), unit, unit->getPlayer()->getName().c_str());

	InformationManager::Instance().onUnitRenegade(unit);
}

void GameCommander::onUnitMorph(BWAPI::Unit unit)
{ 
	InformationManager::Instance().onUnitMorph(unit);

	// Zerg ���� Worker �� Morph �� ���� ó��
	WorkerManager::Instance().onUnitMorph(unit);
}

void GameCommander::onUnitDiscover(BWAPI::Unit unit)
{
}

void GameCommander::onUnitEvade(BWAPI::Unit unit)
{
}

void GameCommander::onSendText(std::string text)
{
}

void GameCommander::onReceiveText(BWAPI::Player player, std::string text)
{
}

