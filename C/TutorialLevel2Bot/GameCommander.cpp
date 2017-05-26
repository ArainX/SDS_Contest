#include "GameCommander.h"

using namespace MyBot;

GameCommander::GameCommander()
{
}

void GameCommander::onStart() 
{
}

void GameCommander::onEnd(bool isWinner)
{
}

void GameCommander::onFrame()
{
	// �Ʊ� ���̽� ��ġ. ���� ���̽� ��ġ ������ ����/������Ʈ�Ѵ�
	InformationManager::Instance().update();
		
	// �÷��̾� ���� ǥ�� - InformationManager �� ������� ���
	BWAPI::Broodwar->drawTextScreen(5, 5, "My Player: %c%s (%s) ",
		BWAPI::Broodwar->self()->getTextColor(), BWAPI::Broodwar->self()->getName().c_str(), InformationManager::Instance().selfRace.c_str());
	BWAPI::Broodwar->drawTextScreen(5, 15, "Enemy Player: %c%s (%s)",
		BWAPI::Broodwar->enemy()->getTextColor(), BWAPI::Broodwar->enemy()->getName().c_str(), InformationManager::Instance().enemyRace.c_str());

	// ���� FrameCount ǥ��
	BWAPI::Broodwar->drawTextScreen(300, 100, "FrameCount: %d", BWAPI::Broodwar->getFrameCount());

	// ���� id ǥ��
	for (auto & unit : BWAPI::Broodwar->getAllUnits()) {
		BWAPI::Broodwar->drawTextMap(unit->getPosition().x, unit->getPosition().y, "%d", unit->getID());
	}

	// �÷��̾� Start Location ǥ�� - InformationManager �� ������� ���
	if (InformationManager::Instance()._mainBaseLocations[BWAPI::Broodwar->self()]) {
		BWAPI::Broodwar->drawTextScreen(200, 5, "Start Location: %d, %d",
			InformationManager::Instance()._mainBaseLocations[BWAPI::Broodwar->self()]->getTilePosition().x,
			InformationManager::Instance()._mainBaseLocations[BWAPI::Broodwar->self()]->getTilePosition().y);
	}
	if (InformationManager::Instance()._mainBaseLocations[BWAPI::Broodwar->enemy()]) {
		BWAPI::Broodwar->drawTextScreen(200, 15, "Start Location: %d, %d",
			InformationManager::Instance()._mainBaseLocations[BWAPI::Broodwar->enemy()]->getTilePosition().x,
			InformationManager::Instance()._mainBaseLocations[BWAPI::Broodwar->enemy()]->getTilePosition().y);
	}
}

void GameCommander::onUnitShow(BWAPI::Unit unit)			
{ 
}

void GameCommander::onUnitHide(BWAPI::Unit unit)			
{
}

void GameCommander::onUnitCreate(BWAPI::Unit unit)		
{ 
}

void GameCommander::onUnitComplete(BWAPI::Unit unit)
{
}

void GameCommander::onUnitDestroy(BWAPI::Unit unit)		
{
}

void GameCommander::onUnitRenegade(BWAPI::Unit unit)
{
}

void GameCommander::onUnitMorph(BWAPI::Unit unit)
{ 
}

void GameCommander::onUnitDiscover(BWAPI::Unit unit)
{
}

void GameCommander::onUnitEvade(BWAPI::Unit unit)
{
}

