#include "MyBotModule.h"

using namespace BWAPI;
using namespace BWTA;
using namespace MyBot;

MyBotModule::MyBotModule(){
}

MyBotModule::~MyBotModule(){
}

void MyBotModule::onStart(){
	
	if (BWAPI::Broodwar->isReplay()) {
		return;
	}

	time_t t;
	srand((unsigned int)(time(&t)));

	// Config ���� ������ ���ŷӰ�, ���� �� ���� Config ���� ��ġ�� �������ִ� ���� ���ŷӱ� ������, 
	// Config �� ���Ϸκ��� �о������ �ʰ�, Config Ŭ������ ���� ����ϵ��� �մϴ�.
	if (Config::BWAPIOptions::EnableCompleteMapInformation)
	{
		BWAPI::Broodwar->enableFlag(BWAPI::Flag::CompleteMapInformation);
	}

	if (Config::BWAPIOptions::EnableUserInput)
	{
		BWAPI::Broodwar->enableFlag(BWAPI::Flag::UserInput);
	}

	Broodwar->setCommandOptimizationLevel(1);


	// Speedups for automated play, sets the number of milliseconds bwapi spends in each frame
	// Fastest: 42 ms/frame.  1�ʿ� 24 frame. �Ϲ������� 1�ʿ� 24frame�� ���� ���Ӽӵ��� �մϴ�
	// Normal: 67 ms/frame. 1�ʿ� 15 frame
	// As fast as possible : 0 ms/frame. CPU�� �Ҽ��ִ� ���� ���� �ӵ�. 
	BWAPI::Broodwar->setLocalSpeed(Config::BWAPIOptions::SetLocalSpeed);
	// frameskip�� �ø��� ȭ�� ǥ�õ� ������Ʈ ���ϹǷ� �ξ� �����ϴ�
	BWAPI::Broodwar->setFrameSkip(Config::BWAPIOptions::SetFrameSkip);
	
	std::cout << "Map analyzing started" << std::endl;
	BWTA::readMap();
	BWTA::analyze();
	BWTA::buildChokeNodes();
	std::cout << "Map analyzing finished" << std::endl;

	gameCommander.onStart();
}

void MyBotModule::onEnd(bool isWinner){
	if (isWinner)
		std::cout << "I won the game" << std::endl;
	else
		std::cout << "I lost the game" << std::endl;

	gameCommander.onEnd(isWinner);
}

void MyBotModule::onFrame(){
	if (BWAPI::Broodwar->isReplay()) {
		return;
	}

	gameCommander.onFrame();

	// ȭ�� ��� �� ����� �Է� ó��
	UXManager::Instance().update();
}

void MyBotModule::onSendText(std::string text){

	ParseTextCommand(text);

	gameCommander.onSendText(text);

	// Display the text to the game 
	BWAPI::Broodwar->sendText("%s", text.c_str());
}

void MyBotModule::onReceiveText(BWAPI::Player player, std::string text){
	BWAPI::Broodwar << player->getName() << " said \"" << text << "\"" << std::endl;

	gameCommander.onReceiveText(player, text);
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
	else
		BWAPI::Broodwar << "Nuclear Launch Detected" << std::endl;

}

void MyBotModule::onUnitCreate(BWAPI::Unit unit){
	if (!BWAPI::Broodwar->isReplay()) {
		gameCommander.onUnitCreate(unit);
	}
	else
	{
		// if we are in a replay, then we will print out the build order
		// (just of the buildings, not the units).
		if (unit->getType().isBuilding() && unit->getPlayer()->isNeutral() == false)
		{
			int seconds = BWAPI::Broodwar->getFrameCount() / 24;
			int minutes = seconds / 60;
			seconds %= 60;
			BWAPI::Broodwar->sendText("%.2d:%.2d: %s creates a %s", minutes, seconds, unit->getPlayer()->getName().c_str(), unit->getType().c_str());
		}
	}
}

void MyBotModule::onUnitMorph(BWAPI::Unit unit){
	if (!BWAPI::Broodwar->isReplay()){
		gameCommander.onUnitMorph(unit);
	}
	else {
		// if we are in a replay, then we will print out the build order
		// (just of the buildings, not the units).
		if (unit->getType().isBuilding() && unit->getPlayer()->isNeutral() == false)
		{
			int seconds = BWAPI::Broodwar->getFrameCount() / 24;
			int minutes = seconds / 60;
			seconds %= 60;
			BWAPI::Broodwar->sendText("%.2d:%.2d: %s morphs a %s", minutes, seconds, unit->getPlayer()->getName().c_str(), unit->getType().c_str());
		}
	}
}

void MyBotModule::onUnitDestroy(BWAPI::Unit unit){
	if (!BWAPI::Broodwar->isReplay()){

		// �й� ���� üũ �� GG
		int buildingCount = 0;
		int workerCount = 0;

		for (auto & unit : BWAPI::Broodwar->self()->getUnits()) {
			if (unit->getType().isBuilding()) {
				buildingCount++;
			}
			else if (unit->getType().isWorker()) {
				workerCount++;
			}
		}
		if (buildingCount == 0) {
			BWAPI::Broodwar->sendText("GG");
			BWAPI::Broodwar->leaveGame();
		}

		gameCommander.onUnitDestroy(unit);
	}
}

void MyBotModule::onUnitShow(BWAPI::Unit unit){
	if (!BWAPI::Broodwar->isReplay()) {
		gameCommander.onUnitShow(unit);
	}
}

void MyBotModule::onUnitHide(BWAPI::Unit unit){
	if (!BWAPI::Broodwar->isReplay()) {
		gameCommander.onUnitHide(unit);
	}
}

void MyBotModule::onUnitRenegade(BWAPI::Unit unit){
	if (!BWAPI::Broodwar->isReplay()) {
		gameCommander.onUnitRenegade(unit);
	}
}

void MyBotModule::onUnitDiscover(BWAPI::Unit unit){
	if (!BWAPI::Broodwar->isReplay()) {
		gameCommander.onUnitDiscover(unit);
	}
}

void MyBotModule::onUnitEvade(BWAPI::Unit unit){
	if (!BWAPI::Broodwar->isReplay()) {
		gameCommander.onUnitEvade(unit);
	}
}

void MyBotModule::onUnitComplete(BWAPI::Unit unit){
	if (!BWAPI::Broodwar->isReplay()) {
		gameCommander.onUnitComplete(unit);
	}
}

void MyBotModule::onSaveGame(std::string gameName){
	BWAPI::Broodwar->sendText("The game was saved to \"%s\".", gameName.c_str());
}


void MyBotModule::ParseTextCommand(const std::string & commandString)
{
	// Make sure to use %s and pass the text as a parameter,
	// otherwise you may run into problems when you use the %(percent) character!

	BWAPI::Player self = BWAPI::Broodwar->self();

	if (commandString == "/afap") {
		BWAPI::Broodwar->setLocalSpeed(0);
		BWAPI::Broodwar->setFrameSkip(0);
	}
	else if (commandString == "/fast") {
		BWAPI::Broodwar->setLocalSpeed(24);
		BWAPI::Broodwar->setFrameSkip(0);
	}
	else if (commandString == "/slow") {
		BWAPI::Broodwar->setLocalSpeed(42);
		BWAPI::Broodwar->setFrameSkip(0);
	}
	else if (commandString == "/endthegame") {
		//bwapi->setFrameSkip(16);   // Not needed if using setGUI(false).
		BWAPI::Broodwar->setGUI(false);
	}
}
