#include "MyBotModule.h"

using namespace BWAPI;
using namespace BWTA;
using namespace MyBot;

MyBotModule::MyBotModule(){
}

MyBotModule::~MyBotModule(){
}

void MyBotModule::onStart(){
	// ���÷��� ����� ��� �ƹ��͵� ���� ����
	if (BWAPI::Broodwar->isReplay()) {
		return;
	}

	// ���� �õ尪 ����
	time_t t;
	srand((unsigned int)(time(&t)));

	// ��ü ���� �� ����� �̺�Ʈ�� �� �ľ��ϴ� ���
	//BWAPI::Broodwar->enableFlag(BWAPI::Flag::CompleteMapInformation);
	// Ű����/���콺�� ���� �÷��̸� ������ �� �ִ� ���
	BWAPI::Broodwar->enableFlag(BWAPI::Flag::UserInput);
	
	// ������ ���� ����� �ϳ��� ó���ؼ� CPU �δ��� �ٿ���
	Broodwar->setCommandOptimizationLevel(1);

	// Sets the number of milliseconds bwapi spends in each frame
	// Fastest: 42 ms/frame.  1�ʿ� �� 24 frame (��Ȯ���� 23.80952380952381 frame). ���� �Ϲ����� ���� �ӵ�
	// Normal: 67 ms/frame. 1�ʿ� �� 15 frame
	// As fast as possible : 0 ms/frame. CPU�� �Ҽ��ִ� ���� ���� �ӵ�. 
	// ���� �ÿ��� 1�ʿ� 15 frame �� ���Ӽӵ��� �Ѵ�
	BWAPI::Broodwar->setLocalSpeed(20);
	// frameskip�� �ø��� ȭ�� ǥ�õ� ������Ʈ ���ϹǷ� �ξ� ������
	BWAPI::Broodwar->setFrameSkip(0);
	
	std::cout << "Map analyzing started" << std::endl;
	BWTA::readMap();
	BWTA::analyze();
	BWTA::buildChokeNodes();
	std::cout << "Map analyzing finished" << std::endl;
}

void MyBotModule::onEnd(bool isWinner){
	if (isWinner)
		std::cout << "I won the game" << std::endl;
	else
		std::cout << "I lost the game" << std::endl;
}

void MyBotModule::onFrame(){

	// ��� �������� 500 frame �� �Ǿ��� �� 1���� ǥ��
	if (BWAPI::Broodwar->getFrameCount() == 500) {

		// ��� ������Ʈ�� ǥ��
		std::cout << "Hello Starcraft command prompt" << std::endl;

		// ���� ȭ�鿡 ǥ��
		BWAPI::Broodwar << "Hello Starcraft game screen" << std::endl;
	}
}

void MyBotModule::onSendText(std::string text){
	BWAPI::Broodwar->sendText("%s", text.c_str());

	// Display the text to the game 
	BWAPI::Broodwar << text << std::endl;
}

void MyBotModule::onReceiveText(BWAPI::Player player, std::string text){
	BWAPI::Broodwar << player->getName() << " said \"" << text << "\"" << std::endl;
}

void MyBotModule::onPlayerLeft(BWAPI::Player player){
	BWAPI::Broodwar << player->getName() << " left the game." << std::endl;
}

void MyBotModule::onNukeDetect(BWAPI::Position target){
	if (target != Positions::Unknown)
	{
		BWAPI::Broodwar->drawCircleMap(target, 40, Colors::Red, true);
		BWAPI::Broodwar << "Nuclear Launch Detected at " << target << std::endl;
	}
	else {
		BWAPI::Broodwar << "Nuclear Launch Detected" << std::endl;
	}
}

void MyBotModule::onUnitCreate(BWAPI::Unit unit){
	if (unit->getPlayer()->isNeutral() == false)
	{
		BWAPI::Broodwar << unit->getType().c_str() << " " << unit->getID() << " created at " << unit->getTilePosition().x << ", " << unit->getTilePosition().y << std::endl;
	}
}

void MyBotModule::onUnitMorph(BWAPI::Unit unit){
	if (unit->getPlayer()->isNeutral() == false)
	{
		BWAPI::Broodwar << unit->getType().c_str() << " " << unit->getID() << " morphed at " << unit->getTilePosition().x << "," << unit->getTilePosition().y << std::endl;
	}
}

void MyBotModule::onUnitDestroy(BWAPI::Unit unit){
	if (unit->getPlayer()->isNeutral() == false)
	{
		BWAPI::Broodwar << unit->getType().c_str() << " " << unit->getID() << " destroyed at " << unit->getTilePosition().x << "," << unit->getTilePosition().y << std::endl;
	}
}

void MyBotModule::onUnitShow(BWAPI::Unit unit){
	if (unit->getPlayer()->isNeutral() == false)
	{
		BWAPI::Broodwar << unit->getType().c_str() << " " << unit->getID() << " showed at " << unit->getTilePosition().x << "," << unit->getTilePosition().y << std::endl;
	}
}

void MyBotModule::onUnitHide(BWAPI::Unit unit){
	if (unit->getPlayer()->isNeutral() == false)
	{
		BWAPI::Broodwar << unit->getType().c_str() << " " << unit->getID() << " hid at " << unit->getTilePosition().x << "," << unit->getTilePosition().y << std::endl;
	}
}

void MyBotModule::onUnitRenegade(BWAPI::Unit unit){
	BWAPI::Broodwar << unit->getType().c_str() << " " << unit->getID() << " renegaded at " << unit->getTilePosition().x << "," << unit->getTilePosition().y << std::endl;
}

void MyBotModule::onUnitDiscover(BWAPI::Unit unit){
	if (unit->getPlayer()->isNeutral() == false)
	{
		BWAPI::Broodwar << unit->getType().c_str() << " " << unit->getID() << " discovered at " << unit->getTilePosition().x << "," << unit->getTilePosition().y << std::endl;
	}
}

void MyBotModule::onUnitEvade(BWAPI::Unit unit){
	if (unit->getPlayer()->isNeutral() == false)
	{
		BWAPI::Broodwar << unit->getType().c_str() << " " << unit->getID() << " evaded at " << unit->getTilePosition().x << "," << unit->getTilePosition().y << std::endl;
	}
}

void MyBotModule::onUnitComplete(BWAPI::Unit unit){
	if (unit->getPlayer()->isNeutral() == false)
	{
		BWAPI::Broodwar << unit->getType().c_str() << " " << unit->getID() << " completed at " << unit->getTilePosition().x << "," << unit->getTilePosition().y << std::endl;
	}
}

void MyBotModule::onSaveGame(std::string gameName){
	BWAPI::Broodwar->sendText("The game was saved to \"%s\".", gameName.c_str());
}


