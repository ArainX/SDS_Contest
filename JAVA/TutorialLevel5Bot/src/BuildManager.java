import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import bwapi.Pair;
import bwapi.Position;
import bwapi.Race;
import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitCommand;
import bwapi.UnitCommandType;
import bwapi.UnitType;
import bwapi.UpgradeType;

/// ����(�ǹ� �Ǽ� / ���� �Ʒ� / ��ũ ����ġ / ���׷��̵�) ����� ���������� �����ϱ� ���� ���� ť�� �����ϰ�, ���� ť�� �ִ� ����� �ϳ��� �����ϴ� class
/// ���� ��� �� �ǹ� �Ǽ� ����� ConstructionManager�� �����մϴ�
/// @see ConstructionManager
public class BuildManager {

	/// BuildOrderItem ���� ����� �����ϴ� buildQueue 
	public BuildOrderQueue buildQueue = new BuildOrderQueue();

	private static BuildManager instance = new BuildManager();

	/// static singleton ��ü�� �����մϴ�
	public static BuildManager Instance() {
		return instance;
	}

	/// buildQueue �� ���� Dead lock �� ������ �����ϰ�, ���� �켱������ ���� BuildOrderItem �� ����ǵ��� �õ��մϴ�
	public void update() {
		// 1��(24������)�� 4�� ������ �����ص� ����ϴ�
		if (MyBotModule.Broodwar.getFrameCount() % 6 != 0)
			return;

		if (buildQueue.isEmpty()) {
			return;
		}

		// Dead Lock �� üũ�ؼ� �����Ѵ�
		checkBuildOrderQueueDeadlockAndAndFixIt();
		// Dead Lock ������ Empty �� �� �ִ�
		if (buildQueue.isEmpty()) {
			return;
		}

		// the current item to be used
		BuildOrderItem currentItem = buildQueue.getHighestPriorityItem();

		//System.out.println("current HighestPriorityItem is " + currentItem.metaType.getName());

		// while there is still something left in the buildQueue
		while (!buildQueue.isEmpty()) {
			boolean isOkToRemoveQueue = true;

			// this is the unit which can produce the currentItem
			Unit producer = getProducer(currentItem.metaType, null);
			/*
			 * if (currentItem.metaType.isUnit() &&
			 * currentItem.metaType.getUnitType().isBuilding()) { if (producer
			 * != null) { System.out.println("Build " +
			 * currentItem.metaType.getName() + " producer : " +
			 * producer.getType() + " ID : " + producer.getID()); } else {
			 * System.out.println("Build " + currentItem.metaType.getName() +
			 * " producer null"); } }
			 */

			Unit secondProducer = null;
			boolean canMake = false;

			// �ǹ��� ����� �ִ� ����(�ϲ�)�̳�, ������ ����� �ִ� ����(�ǹ� or ����)�� ������
			if (producer != null) {

				// check to see if we can make it right now
				// ���� �ش� ������ �Ǽ�/���� �� �� �ִ����� ���� �ڿ�, ���ö���, ��ũ Ʈ��, producer ���� ����
				// �Ǵ��Ѵ�
				canMake = canMakeNow(producer, currentItem.metaType);

				/*
				 * if (currentItem.metaType.isUnit() &&
				 * currentItem.metaType.getUnitType().isBuilding() ) { std::cout
				 * + "Build " + currentItem.metaType.getName() +
				 * " canMakeNow : " + canMake + std::endl; }
				 */

				// �����佺 ���� ���� �� Protoss_Archon / Protoss_Dark_Archon �� ����
				// Protoss_High_Templar / Protoss_Dark_Templar �� ������ ��ü��Ű�� �����
				// �Ἥ ����� ������
				// secondProducer �� �߰��� ã�� Ȯ���Ѵ�
				if (canMake) {
					if (currentItem.metaType.isUnit()) {
						if (currentItem.metaType.getUnitType() == UnitType.Protoss_Archon
								|| currentItem.metaType.getUnitType() == UnitType.Protoss_Dark_Archon) {
							secondProducer = getAnotherProducer(producer, producer.getPosition());
							if (secondProducer == null) {
								canMake = false;
							}
						}
					}
				}
			}

			// if we can make the current item, create it
			if (producer != null && canMake == true) {
				MetaType t = currentItem.metaType;

				if (t.isUnit()) {
					if (t.getUnitType().isBuilding()) {

						// ���� ���� �ǹ� �� Zerg_Lair, Zerg_Hive, Zerg_Greater_Spire,
						// Zerg_Sunken_Colony, Zerg_Spore_Colony �� ���� �ǹ��� Morph
						// ���� �����
						// Morph�� �����ϸ� isMorphing = true, isBeingConstructed =
						// true, isConstructing = true �� �ǰ�
						// �ϼ��Ǹ� isMorphing = false, isBeingConstructed = false,
						// isConstructing = false, isCompleted = true �� �ȴ�
						if (t.getUnitType().getRace() == Race.Zerg && t.getUnitType().whatBuilds().first.isBuilding()) {
							producer.morph(t.getUnitType());
						}
						// �׶� Addon �ǹ��� ��� (Addon �ǹ��� ������ �ִ����� getProducer �Լ�����
						// �̹� üũ�Ϸ�)
						// ��ǹ��� Addon �ǹ� ���� ������ canBuildAddon = true,
						// isConstructing = false, canCommand = true �̴ٰ�
						// Addon �ǹ��� ���� �����ϸ� canBuildAddon = false,
						// isConstructing = true, canCommand = true �� �ǰ� (Addon
						// �ǹ� �Ǽ� ��Ҵ� �����ϳ� Train �� Ŀ�ǵ�� �Ұ���)
						// �ϼ��Ǹ� canBuildAddon = false, isConstructing = false ��
						// �ȴ�
						else if (t.getUnitType().isAddon()) {

							// std::cout + "addon build start " + std::endl;

							producer.buildAddon(t.getUnitType());
							// �׶� Addon �ǹ��� ��� ���������� buildAddon ����� ������ SCV�� ��ǹ�
							// ��ó�� ���� �� �ѵ��� buildAddon ����� ��ҵǴ� ��찡 �־
							// ��ǹ��� isConstructing = true ���·� �ٲ� ���� Ȯ���� ��
							// buildQueue ���� �����ؾ��Ѵ�
							if (producer.isConstructing() == false) {
								isOkToRemoveQueue = false;
							}
							// std::cout + "8";
						}
						// �׿� ��κ� �ǹ��� ���
						else {
							// ConstructionPlaceFinder �� ���� �Ǽ� ���� ��ġ
							// desiredPosition �� �˾Ƴ���
							// ConstructionManager �� ConstructionTask Queue�� �߰���
							// �ؼ� desiredPosition �� �Ǽ��� �ϰ� �Ѵ�.
							// ConstructionManager �� �Ǽ� ���߿� �ش� ��ġ�� �Ǽ��� ��������� �ٽ�
							// ConstructionPlaceFinder �� ���� �Ǽ� ���� ��ġ��
							// desiredPosition �������� ã�� ���̴�
							TilePosition desiredPosition = getDesiredPosition(t.getUnitType(), currentItem.seedLocation,
									currentItem.seedLocationStrategy);

							// if (Config::Debug::LogToConsole) std::cout +
							// "BuildManager "
							// +
							// currentItem.metaType.getUnitType().getName().c_str()
							// + " desiredPosition " + desiredPosition.x + "," +
							// desiredPosition.y + std::endl;

							if (desiredPosition != TilePosition.None) {
								// Send the construction task to the
								// construction manager
								ConstructionManager.Instance().addConstructionTask(t.getUnitType(), desiredPosition);
							} else {
								// �ǹ� ���� ��ġ�� ���� ����, Protoss_Pylon �� ���ų�, Creep
								// �� ���ų�, Refinery �� �̹� �� �������ְų�, ���� ���� ������ ������
								// ���� ����ε�,
								// ��κ��� ��� Pylon �̳� Hatchery�� �������� �ִ� ���̹Ƿ�, ����
								// frame �� �ǹ� ���� ������ �ٽ� Ž���ϵ��� �Ѵ�.
								if (Config.LogToConsole) {

									System.out.print(
											"There is no place to construct " + currentItem.metaType.getUnitType()
													+ " strategy " + currentItem.seedLocationStrategy);
									if (currentItem.seedLocation != null)
										System.out.print(" seedPosition " + currentItem.seedLocation.getX() + ","
												+ currentItem.seedLocation.getY());
									if (desiredPosition != null)
										System.out.print(" desiredPosition " + desiredPosition.getX() + ","
												+ desiredPosition.getY());
								}
								isOkToRemoveQueue = false;
							}
						}
					}
					// �������� / ���������� ���
					else {
						// ���� �������� / ��������
						if (t.getUnitType().getRace() == Race.Zerg) {
							// ���� ���� ������ ���� ��κ��� Morph ���� �����
							if (t.getUnitType() != UnitType.Zerg_Infested_Terran) {
								producer.morph(t.getUnitType());
							}
							// ���� ���� ���� �� Zerg_Infested_Terran �� Train ���� �����
							else {
								producer.train(t.getUnitType());
							}
						}
						// �����佺 �������� / ��������
						else if (t.getUnitType().getRace() == Race.Protoss) {
							// �����佺 ���� ���� �� Protoss_Archon �� ����
							// Protoss_High_Templar �� ������ ��ü��Ű�� ����� �Ἥ �����
							if (t.getUnitType() == UnitType.Protoss_Archon) {
								producer.useTech(TechType.Archon_Warp, secondProducer);
							}
							// �����佺 ���� ���� �� Protoss_Dark_Archon �� ����
							// Protoss_Dark_Templar �� ������ ��ü��Ű�� ����� �Ἥ �����
							else if (t.getUnitType() == UnitType.Protoss_Dark_Archon) {
								producer.useTech(TechType.Dark_Archon_Meld, secondProducer);
							} else {
								producer.train(t.getUnitType());
							}
						}
						// �׶� �������� / ��������
						else {
							producer.train(t.getUnitType());
						}
					}
				}
				// if we're dealing with a tech research
				else if (t.isTech()) {
					producer.research(t.getTechType());
				} else if (t.isUpgrade()) {
					producer.upgrade(t.getUpgradeType());
				}

				//System.out.println(" build " + t.getName() + " started ");

				// remove it from the buildQueue
				if (isOkToRemoveQueue) {
					buildQueue.removeCurrentItem();
				}

				// don't actually loop around in here
				break;
			}
			// otherwise, if we can skip the current item
			else if (buildQueue.canSkipCurrentItem()) {
				// skip it and get the next one
				buildQueue.skipCurrentItem();
				currentItem = buildQueue.getNextItem();
			} else {
				// so break out
				break;
			}
		}
	}

	/// �ش� MetaType �� build �� �� �ִ� producer �� ã�� ��ȯ�մϴ�
	/// @param t �����Ϸ��� ����� Ÿ��
	/// @param closestTo �Ķ��Ÿ �Է� �� producer �ĺ��� �� �ش� position ���� ���� ����� producer �� �����մϴ�
	/// @param producerID �Ķ��Ÿ �Է� �� �ش� ID�� unit �� producer �ĺ��� �� �� �ֽ��ϴ�
	public Unit getProducer(MetaType t, Position closestTo, int producerID) {
		// get the type of unit that builds this
		UnitType producerType = t.whatBuilds();

		// make a set of all candidate producers
		List<Unit> candidateProducers = new ArrayList<Unit>();
		for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
			if (unit == null)
				continue;

			// reasons a unit can not train the desired type
			if (unit.getType() != producerType) {
				continue;
			}
			if (!unit.exists()) {
				continue;
			}
			if (!unit.isCompleted()) {
				continue;
			}
			if (unit.isTraining()) {
				continue;
			}
			if (!unit.isPowered()) {
				continue;
			}
			// if unit is lifted, unit should land first
			if (unit.isLifted()) {
				continue;
			}
			
			

			if (t.isUnit()) {
				// if the type requires an addon and the producer doesn't have
				// one
				// C++ : typedef std::pair<BWAPI::UnitType, int> ReqPair;
				Pair<UnitType, Integer> ReqPair = null;

				Map<UnitType, Integer> requiredUnitsMap = t.getUnitType().requiredUnits();
				if (requiredUnitsMap != null) {
					Iterator<UnitType> it = requiredUnitsMap.keySet().iterator();

					// for (final Pair<UnitType, Integer> pair :
					// t.getUnitType().requiredUnits())
					while (it.hasNext()) {
						UnitType requiredType = it.next();
						if (requiredType.isAddon()) {
							if (unit.getAddon() == null || (unit.getAddon().getType() != requiredType)) {
								continue;
							}
						}
					}
				}

				// if the type is an addon
				if (t.getUnitType().isAddon()) {
					// if the unit already has an addon, it can't make one
					if (unit.getAddon() != null) {
						continue;
					}

					// ��ǹ��� �Ǽ��ǰ� �ִ� �߿��� isCompleted = false, isConstructing =
					// true, canBuildAddon = false �̴ٰ�
					// �Ǽ��� �ϼ��� �� �� �����ӵ����� isCompleted = true ������, canBuildAddon
					// = false �� ��찡 �ִ�
					if (!unit.canBuildAddon()) {
						continue;
					}

					// if we just told this unit to build an addon, then it will
					// not be building another one
					// this deals with the frame-delay of telling a unit to
					// build an addon and it actually starting to build
					if (unit.getLastCommand().getUnitCommandType() == UnitCommandType.Build_Addon // C++
																									// :
																									// unit.getLastCommand().getType()
							&& (MyBotModule.Broodwar.getFrameCount() - unit.getLastCommandFrame() < 10)) {
						continue;
					}

					boolean isBlocked = false;

					// if the unit doesn't have space to build an addon, it
					// can't make one
					TilePosition addonPosition = new TilePosition(
							unit.getTilePosition().getX() + unit.getType().tileWidth(),
							unit.getTilePosition().getY() + unit.getType().tileHeight() - t.getUnitType().tileHeight());

					for (int i = 0; i < t.getUnitType().tileWidth(); ++i) {
						for (int j = 0; j < t.getUnitType().tileHeight(); ++j) {
							TilePosition tilePos = new TilePosition(addonPosition.getX() + i, addonPosition.getY() + j);

							// if the map won't let you build here, we can't
							// build it.
							// �� Ÿ�� ��ü�� �Ǽ� �Ұ����� Ÿ���� ��� + ���� �ǹ��� �ش� Ÿ�Ͽ� �̹� �ִ°��
							if (!MyBotModule.Broodwar.isBuildable(tilePos, true)) {
								isBlocked = true;
							}

							// if there are any units on the addon tile, we
							// can't build it
							// �Ʊ� ������ Addon ���� ��ġ�� �־ ������. (���� ������ Addon ���� ��ġ��
							// ������ �Ǽ� �ȵǴ����� ���� ��Ȯ����)
							for (Unit u : MyBotModule.Broodwar.getUnitsOnTile(tilePos.getX(), tilePos.getY())) {
								//System.out.println("Construct " + t.getName() + " beside " + unit.getType() + "("
								//		+ unit.getID() + ")" + ", units on Addon Tile " + tilePos.getX() + ","
								//		+ tilePos.getY() + " is " + u.getType() + "(ID : " + u.getID() + " Player : "
								//		+ u.getPlayer().getName() + ")");
								if (u.getPlayer() != InformationManager.Instance().getSelf()) {
									isBlocked = false;
								}
							}
						}
					}

					if (isBlocked) {
						continue;
					}
				}
			}

			// if we haven't cut it, add it to the set of candidates
			candidateProducers.add(unit); // C++ :
											// candidateProducers.insert(unit);

		}

		return getClosestUnitToPosition(candidateProducers, closestTo);
	}

	/// �ش� MetaType �� build �� �� �ִ� producer �� ã�� ��ȯ�մϴ�
	public Unit getProducer(MetaType t, Position closestTo) {
		return getProducer(t, closestTo, -1);
	}

	/// �ش� MetaType �� build �� �� �ִ� producer �� ã�� ��ȯ�մϴ�
	public Unit getProducer(MetaType t) {
		return getProducer(t, Position.None, -1);
	}

	/// �ش� MetaType �� build �� �� �ִ�, getProducer ���ϰ��� �ٸ� producer �� ã�� ��ȯ�մϴ�
	/// �����佺 ���� ���� �� Protoss_Archon / Protoss_Dark_Archon �� ������ �� ����մϴ�
	public Unit getAnotherProducer(Unit producer, Position closestTo) {
		if (producer == null)
			return null;

		Unit closestUnit = null;

		List<Unit> candidateProducers = new ArrayList<Unit>();
		for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
			if (unit == null) {
				continue;
			}
			if (unit.getType() != producer.getType()) {
				continue;
			}
			if (unit.getID() == producer.getID()) {
				continue;
			}
			if (!unit.isCompleted()) {
				continue;
			}
			if (unit.isTraining()) {
				continue;
			}
			if (!unit.exists()) {
				continue;
			}
			if (unit.getHitPoints() + unit.getEnergy() <= 0) {
				continue;
			}

			candidateProducers.add(unit); // C++ :
											// candidateProducers.insert(unit);
		}

		return getClosestUnitToPosition(candidateProducers, closestTo);
	}

	public Unit getClosestUnitToPosition(final List<Unit> units, Position closestTo) {
		if (units.size() == 0) {
			return null;
		}

		// if we don't care where the unit is return the first one we have
		if (closestTo == Position.None) {
			return units.get(0); // C++ : return units.begin();
		}

		Unit closestUnit = null;
		double minDist = 1000000000;

		for (Unit unit : units) {
			if (unit == null)
				continue;

			double distance = unit.getDistance(closestTo);
			if (closestUnit == null || distance < minDist) {
				closestUnit = unit;
				minDist = distance;
			}
		}

		return closestUnit;
	}

	// ���� �ش� ������ �Ǽ�/���� �� �� �ִ����� ���� �ڿ�, ���ö���, ��ũ Ʈ��, producer ���� ���� �Ǵ��Ѵ�
	// �ش� ������ �ǹ��� ��� �ǹ� ���� ��ġ�� ���� ���� (Ž���߾��� Ÿ������, �Ǽ� ������ Ÿ������, ������ Pylon�� �ִ���,
	// Creep�� �ִ� ������ ��) �� �Ǵ����� �ʴ´�
	public boolean canMakeNow(Unit producer, MetaType t) {
		if (producer == null) {
			return false;
		}

		boolean canMake = hasEnoughResources(t);

		if (canMake) {
			if (t.isUnit()) {
				// MyBotModule.Broodwar.canMake : Checks all the requirements
				// include resources, supply, technology tree, availability, and
				// required units
				canMake = MyBotModule.Broodwar.canMake(t.getUnitType(), producer);
			} else if (t.isTech()) {
				canMake = MyBotModule.Broodwar.canResearch(t.getTechType(), producer);
			} else if (t.isUpgrade()) {
				canMake = MyBotModule.Broodwar.canUpgrade(t.getUpgradeType(), producer);
			}
		}

		return canMake;
	}

	public boolean canMake(MetaType t) {
		boolean canMake = false;

		if (canMake) {
			if (t.isUnit()) {
				// Checks all the requirements include resources, supply,
				// technology tree, availability, and required units
				canMake = MyBotModule.Broodwar.canMake(t.getUnitType());
			} else if (t.isTech()) {
				canMake = MyBotModule.Broodwar.canResearch(t.getTechType());
			} else if (t.isUpgrade()) {
				canMake = MyBotModule.Broodwar.canUpgrade(t.getUpgradeType());
			}
		}

		return canMake;

	}

	// �Ǽ� ���� ��ġ�� ã�´�
	// seedLocationStrategy �� SeedPositionSpecified �� ��쿡�� �� ��ó�� ã�ƺ���,
	// SeedPositionSpecified �� �ƴ� ��쿡�� seedLocationStrategy �� ���ݾ� �ٲ㰡�� ��� ã�ƺ���.
	// (MainBase . MainBase ���� . MainBase ��� . MainBase ����� �ո��� . MainBase �����
	// �ո����� ��� . �ٸ� ��Ƽ ��ġ . Ž�� ����)
	public TilePosition getDesiredPosition(UnitType unitType, TilePosition seedPosition,
			BuildOrderItem.SeedPositionStrategy seedPositionStrategy) {
		TilePosition desiredPosition = ConstructionPlaceFinder.Instance()
				.getBuildLocationWithSeedPositionAndStrategy(unitType, seedPosition, seedPositionStrategy);

		/*
		 * if (Config::Debug::LogToConsole) std::cout +
		 * "ConstructionPlaceFinder getBuildLocationWithSeedPositionAndStrategy "
		 * + unitType.getName().c_str() + " strategy " + seedPositionStrategy +
		 * " seedPosition " + seedPosition.x + "," + seedPosition.y +
		 * " desiredPosition " + desiredPosition.x + "," + desiredPosition.y +
		 * std::endl;
		 */

		// desiredPosition �� ã�� �� ���� ���
		boolean findAnotherPlace = true;
		while (desiredPosition == TilePosition.None) {

			switch (seedPositionStrategy) {
			case MainBaseLocation:
				seedPositionStrategy = BuildOrderItem.SeedPositionStrategy.MainBaseBackYard;
				break;
			case MainBaseBackYard:
				seedPositionStrategy = BuildOrderItem.SeedPositionStrategy.FirstChokePoint;
				break;
			case FirstChokePoint:
				seedPositionStrategy = BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation;
				break;
			case FirstExpansionLocation:
				seedPositionStrategy = BuildOrderItem.SeedPositionStrategy.SecondChokePoint;
				break;
			case SecondChokePoint:
				seedPositionStrategy = BuildOrderItem.SeedPositionStrategy.SecondExpansionLocation;
				break;
			case SecondExpansionLocation:
			case SeedPositionSpecified:
			default:
				findAnotherPlace = false;
				break;
			}

			// �ٸ� ���� �� ã�ƺ���
			if (findAnotherPlace) {
				desiredPosition = ConstructionPlaceFinder.Instance()
						.getBuildLocationWithSeedPositionAndStrategy(unitType, seedPosition, seedPositionStrategy);
				/*
				 * if (Config::Debug::LogToConsole) std::cout +
				 * "ConstructionPlaceFinder getBuildLocationWithSeedPositionAndStrategy "
				 * + unitType.getName().c_str() + " strategy " +
				 * seedPositionStrategy + " seedPosition " + seedPosition.x +
				 * "," + seedPosition.y + " desiredPosition " +
				 * desiredPosition.x + "," + desiredPosition.y + std::endl;
				 */
			}
			// �ٸ� ���� �� ã�ƺ��� �ʰ�, ������
			else {
				break;
			}
		}

		return desiredPosition;
	}

	// ��밡�� �̳׶� = ���� ���� �̳׶� - ����ϱ�� ����Ǿ��ִ� �̳׶�
	public int getAvailableMinerals() {
		return MyBotModule.Broodwar.self().minerals() - ConstructionManager.Instance().getReservedMinerals();
	}

	// ��밡�� ���� = ���� ���� ���� - ����ϱ�� ����Ǿ��ִ� ����
	public int getAvailableGas() {
		return MyBotModule.Broodwar.self().gas() - ConstructionManager.Instance().getReservedGas();
	}

	// return whether or not we meet resources, including building reserves
	public boolean hasEnoughResources(MetaType type) {
		// return whether or not we meet the resources
		return (type.mineralPrice() <= getAvailableMinerals()) && (type.gasPrice() <= getAvailableGas());
	}

	// selects a unit of a given type
	public Unit selectUnitOfType(UnitType type, Position closestTo) {
		// if we have none of the unit type, return null right away
		if (MyBotModule.Broodwar.self().completedUnitCount(type) == 0) {
			return null;
		}

		Unit unit = null;

		// if we are concerned about the position of the unit, that takes
		// priority
		if (closestTo != Position.None) {
			double minDist = 1000000000;

			for (Unit u : MyBotModule.Broodwar.self().getUnits()) {
				if (u.getType() == type) {
					double distance = u.getDistance(closestTo);
					if (unit == null || distance < minDist) {
						unit = u;
						minDist = distance;
					}
				}
			}

			// if it is a building and we are worried about selecting the unit
			// with the least
			// amount of training time remaining
		} else if (type.isBuilding()) {
			for (Unit u : MyBotModule.Broodwar.self().getUnits()) {
				// UAB_ASSERT(u != null, "Unit was null");

				if (u.getType() == type && u.isCompleted() && !u.isTraining() && !u.isLifted() && u.isPowered()) {

					return u;
				}
			}
			// otherwise just return the first unit we come across
		} else {
			for (Unit u : MyBotModule.Broodwar.self().getUnits()) {
				// UAB_ASSERT(u != null, "Unit was null");

				if (u.getType() == type && u.isCompleted() && u.getHitPoints() > 0 && !u.isLifted() && u.isPowered()) {
					return u;
				}
			}
		}

		// return what we've found so far
		return null;
	}

	/// BuildOrderItem ���� ����� �����ϴ� buildQueue �� �����մϴ�
	public BuildOrderQueue getBuildQueue() {
		return buildQueue;
	}

	/// buildQueue �� Dead lock ���θ� �Ǵ��ϱ� ����, ���� �켱������ ���� BuildOrderItem �� producer �� �����ϰԵ� ������ ���θ� �����մϴ�
	public boolean isProducerWillExist(UnitType producerType) {
		boolean isProducerWillExist = true;

		if (MyBotModule.Broodwar.self().completedUnitCount(producerType) == 0
				&& MyBotModule.Broodwar.self().incompleteUnitCount(producerType) == 0) {
			// producer �� �ǹ� �� ��� : �ǹ��� �Ǽ� ������ �߰� �ľ�
			// ������� unitType = Addon �ǹ�. Lair. Hive. Greater Spire. Sunken
			// Colony. Spore Colony. �����佺 �� �׶��� �������� / ��������.
			if (producerType.isBuilding()) {
				if (ConstructionManager.Instance().getConstructionQueueItemCount(producerType, null) == 0) {
					isProducerWillExist = false;
				}
			}
			// producer �� �ǹ��� �ƴ� ��� : producer �� ������ �������� �߰� �ľ�
			// producerType : �ϲ�. Larva. Hydralisk, Mutalisk
			else {
				// Larva �� �ð��� ������ Hatchery, Lair, Hive �κ��� �����Ǳ� ������ �ش� �ǹ��� �ִ���
				// �߰� �ľ�
				if (producerType == UnitType.Zerg_Larva) {
					if (MyBotModule.Broodwar.self().completedUnitCount(UnitType.Zerg_Hatchery) == 0
							&& MyBotModule.Broodwar.self().incompleteUnitCount(UnitType.Zerg_Hatchery) == 0
							&& MyBotModule.Broodwar.self().completedUnitCount(UnitType.Zerg_Lair) == 0
							&& MyBotModule.Broodwar.self().incompleteUnitCount(UnitType.Zerg_Lair) == 0
							&& MyBotModule.Broodwar.self().completedUnitCount(UnitType.Zerg_Hive) == 0
							&& MyBotModule.Broodwar.self().incompleteUnitCount(UnitType.Zerg_Hive) == 0) {
						if (ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Hatchery,
								null) == 0
								&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Lair,
										null) == 0
								&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Hive,
										null) == 0) {
							isProducerWillExist = false;
						}
					}
				}
				// Hydralisk, Mutalisk �� Egg �κ��� �����Ǳ� ������ �߰� �ľ�
				else if (producerType.getRace() == Race.Zerg) {
					boolean isInEgg = false;
					for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
						if (unit.getType() == UnitType.Zerg_Egg && unit.getBuildType() == producerType) {
							isInEgg = true;
						}
						// ���¾ ������ ���� �ݿ��ȵǾ����� �� �־, �߰� ī��Ʈ�� �������
						if (unit.getType() == producerType && unit.isConstructing()) {
							isInEgg = true;
						}
					}
					if (isInEgg == false) {
						isProducerWillExist = false;
					}
				} else {
					isProducerWillExist = false;
				}
			}
		}

		return isProducerWillExist;
	}

	public void checkBuildOrderQueueDeadlockAndAndFixIt() {
		// ��������� ������ �� �ִ� ���������� ���� �Ǵ��Ѵ�
		// this will be true if any unit is on the first frame if it's training
		// time remaining
		// this can cause issues for the build order search system so don't plan
		// a search on these frames
		boolean canPlanBuildOrderNow = true;
		for (final Unit unit : MyBotModule.Broodwar.self().getUnits()) {
			if (unit.getRemainingTrainTime() == 0) {
				continue;
			}

			UnitCommand unitCommand = unit.getLastCommand();
			if (unitCommand != null) {

				UnitCommandType unitCommandType = unitCommand.getUnitCommandType();
				if (unitCommandType != UnitCommandType.None) {
					if (unitCommand.getUnit() != null) {
						UnitType trainType = unitCommand.getUnit().getType();
						if (unit.getRemainingTrainTime() == trainType.buildTime()) {
							canPlanBuildOrderNow = false;
							break;
						}
					}
				}
			}

		}
		if (!canPlanBuildOrderNow) {
			return;
		}

		// BuildQueue �� HighestPriority �� �ִ� BuildQueueItem �� skip �Ұ����� ���ε�,
		// ���������� ������ �� ���ų�, ������ �����ε� ��� �Ұ����� ���, dead lock �� �߻��Ѵ�
		// ���� �ǹ��� BuildQueue�� �߰��س�����, �ش� BuildQueueItem �� �������� ���������� �Ǵ��ؾ� �Ѵ�
		BuildOrderQueue buildQueue = BuildManager.Instance().getBuildQueue();
		if (!buildQueue.isEmpty()) {
			BuildOrderItem currentItem = buildQueue.getHighestPriorityItem();

			// ����
			// if (buildQueue.canSkipCurrentItem() == false)
			if (currentItem.blocking == true) {
				boolean isDeadlockCase = false;

				// producerType�� ���� �˾Ƴ���
				UnitType producerType = currentItem.metaType.whatBuilds();

				// �ǹ��̳� ������ ���
				if (currentItem.metaType.isUnit()) {
					UnitType unitType = currentItem.metaType.getUnitType();
					TechType requiredTechType = unitType.requiredTech();
					final Map<UnitType, Integer> requiredUnits = unitType.requiredUnits();
					int requiredSupply = unitType.supplyRequired();

					/*
					 * std::cout + "To make " + unitType.getName() +
					 * ", producerType " + producerType.getName() +
					 * " completedUnitCount " +
					 * MyBotModule.Broodwar.self().completedUnitCount(
					 * producerType) + " incompleteUnitCount " +
					 * MyBotModule.Broodwar.self().incompleteUnitCount(
					 * producerType) + std::endl;
					 */

					// �ǹ��� �����ϴ� �����̳�, ������ �����ϴ� �ǹ��� �������� �ʰ�, �Ǽ� ���������� ������ dead
					// lock
					if (isProducerWillExist(producerType) == false) {
						isDeadlockCase = true;
					}

					// Refinery �ǹ��� ���, Refinery �� �Ǽ����� ���� Geyser�� �ִ� ��쿡�� ����
					if (!isDeadlockCase && unitType == InformationManager.Instance().getRefineryBuildingType()) {
						boolean hasAvailableGeyser = true;

						// Refinery�� ������ �� �ִ� ��Ҹ� ã�ƺ���
						TilePosition testLocation = getDesiredPosition(unitType, currentItem.seedLocation,
								currentItem.seedLocationStrategy);

						// Refinery �� �������� ��Ҹ� ã�� �� ������ dead lock
						if (testLocation == TilePosition.None || testLocation == TilePosition.Invalid
								|| testLocation.isValid() == false) {
							System.out
									.println("Build Order Dead lock case . Cann't find place to construct " + unitType); // C++
																															// :
																															// unitType.getName()
							hasAvailableGeyser = false;
						} else {
							// Refinery �� �������� ��ҿ� Refinery �� �̹� �Ǽ��Ǿ� �ִٸ� dead
							// lock
							for (Unit u : MyBotModule.Broodwar.getUnitsOnTile(testLocation)) {
								if (u.getType().isRefinery() && u.exists()) {
									hasAvailableGeyser = false;
									break;
								}
							}
						}

						if (hasAvailableGeyser == false) {
							isDeadlockCase = true;
						}
					}

					// ���� ��� ����ġ�� �Ǿ����� �ʰ�, ����ġ �������� ������ dead lock
					if (!isDeadlockCase && requiredTechType != TechType.None) {
						if (MyBotModule.Broodwar.self().hasResearched(requiredTechType) == false) {
							if (MyBotModule.Broodwar.self().isResearching(requiredTechType) == false) {
								isDeadlockCase = true;
							}
						}
					}

					Iterator<UnitType> it = requiredUnits.keySet().iterator();
					// ���� �ǹ�/������ �ִµ�
					if (!isDeadlockCase && requiredUnits.size() > 0) {
						// for (Unit u : it)
						while (it.hasNext()) {
							UnitType requiredUnitType = it.next(); // C++ :
																	// u.first;

							if (requiredUnitType != UnitType.None) {

								/*
								 * std::cout + "pre requiredUnitType " +
								 * requiredUnitType.getName() +
								 * " completedUnitCount " +
								 * MyBotModule.Broodwar.self().
								 * completedUnitCount(requiredUnitType) +
								 * " incompleteUnitCount " +
								 * MyBotModule.Broodwar.self().
								 * incompleteUnitCount(requiredUnitType) +
								 * std::endl;
								 */

								// ���� �ǹ� / ������ �������� �ʰ�, ���� �������� �ʰ�
								if (MyBotModule.Broodwar.self().completedUnitCount(requiredUnitType) == 0
										&& MyBotModule.Broodwar.self().incompleteUnitCount(requiredUnitType) == 0) {
									// ���� �ǹ��� �Ǽ� ���������� ������ dead lock
									if (requiredUnitType.isBuilding()) {
										if (ConstructionManager.Instance()
												.getConstructionQueueItemCount(requiredUnitType, null) == 0) {
											isDeadlockCase = true;
										}
									}
									// ���� ������ Larva �� Zerg ������ ���, Larva,
									// Hatchery, Lair, Hive �� �ϳ��� �������� �ʰ�, �Ǽ�
									// �������� ���� ��쿡 dead lock
									else if (requiredUnitType == UnitType.Zerg_Larva) {
										if (MyBotModule.Broodwar.self().completedUnitCount(UnitType.Zerg_Hatchery) == 0
												&& MyBotModule.Broodwar.self()
														.incompleteUnitCount(UnitType.Zerg_Hatchery) == 0
												&& MyBotModule.Broodwar.self()
														.completedUnitCount(UnitType.Zerg_Lair) == 0
												&& MyBotModule.Broodwar.self()
														.incompleteUnitCount(UnitType.Zerg_Lair) == 0
												&& MyBotModule.Broodwar.self()
														.completedUnitCount(UnitType.Zerg_Hive) == 0
												&& MyBotModule.Broodwar.self()
														.incompleteUnitCount(UnitType.Zerg_Hive) == 0) {
											if (ConstructionManager.Instance()
													.getConstructionQueueItemCount(UnitType.Zerg_Hatchery, null) == 0
													&& ConstructionManager.Instance().getConstructionQueueItemCount(
															UnitType.Zerg_Lair, null) == 0
													&& ConstructionManager.Instance().getConstructionQueueItemCount(
															UnitType.Zerg_Hive, null) == 0) {
												isDeadlockCase = true;
											}
										}
									}
								}
							}
						}
					}

					// �ǹ��� �ƴ� ����/���� ������ ���, ���ö��̰� 400 �� á���� dead lock
					if (!isDeadlockCase && !unitType.isBuilding() && MyBotModule.Broodwar.self().supplyTotal() == 400
							&& MyBotModule.Broodwar.self().supplyUsed() + unitType.supplyRequired() > 400) {
						isDeadlockCase = true;
					}

					// �ǹ��� �ƴ� ����/���� �����ε�, ���ö��̰� �����ϸ� dead lock ��Ȳ�� �Ǳ� ������, 
					// �� ���� ���带 ����ϱ⺸�ٴ�, StrategyManager ��� ���ö��� ���带 �߰������ν� Ǯ���� �Ѵ�
					if (!isDeadlockCase && !unitType.isBuilding()
							&& MyBotModule.Broodwar.self().supplyUsed() + unitType.supplyRequired() > MyBotModule.Broodwar.self().supplyTotal()) 
					{
						//isDeadlockCase = true;
					}

					// Pylon �� �ش� ���� ������ ���� �������� �ϴµ�, Pylon �� �ش� ���� ������ ����, �����Ǿ������� ������ dead lock
					// TODO ���� : Pylon ������ ��Ȯ�ϰ� �ľ��ϴ� ��, Pylon �� �Ǽ� ���� ��, Pylon �� �Ǽ� �Ϸ���� �� �ȵ� �͵� �� �����ؾ� �Ѵ�
					if (!isDeadlockCase && unitType.isBuilding() && unitType.requiresPsi()
							&& currentItem.seedLocationStrategy == BuildOrderItem.SeedPositionStrategy.SeedPositionSpecified) {

						boolean hasFoundPylon = false;
						List<Unit> ourUnits = MyBotModule.Broodwar
								.getUnitsInRadius(currentItem.seedLocation.toPosition(), 4 * Config.TILE_SIZE);

						for (Unit u : ourUnits) {
							if (u.getPlayer() == MyBotModule.Broodwar.self() && u.getType() == UnitType.Protoss_Pylon) {
								hasFoundPylon = true;
							}
						}

						if (hasFoundPylon == false) {
							isDeadlockCase = true;
						}
					}

					// Creep �� �ش� ���� ������ Hatchery�� Creep Colony ���� ���� ���� �������� �ϴµ�, �ش� ���� ������ �������� �ʰ� ������ dead lock
					// TODO ���� : Creep ������ ��Ȯ�ϰ� �ľ��ϴ� ��, Creep Generating �ǹ��� �Ǽ� ���� ��, Creep Generating �ǹ��� �Ǽ� �Ϸ���� �� �ȵ� �͵� �� �����ؾ� �Ѵ�
					if (!isDeadlockCase && unitType.isBuilding() && unitType.requiresCreep()
							&& currentItem.seedLocationStrategy == BuildOrderItem.SeedPositionStrategy.SeedPositionSpecified) {
						boolean hasFoundCreepGenerator = false;
						List<Unit> ourUnits = MyBotModule.Broodwar
								.getUnitsInRadius(currentItem.seedLocation.toPosition(), 4 * Config.TILE_SIZE);

						for (Unit u : ourUnits) {
							if (u.getPlayer() == MyBotModule.Broodwar.self() && (u.getType() == UnitType.Zerg_Hatchery
									|| u.getType() == UnitType.Zerg_Lair || u.getType() == UnitType.Zerg_Hive
									|| u.getType() == UnitType.Zerg_Creep_Colony
									|| u.getType() == UnitType.Zerg_Sunken_Colony
									|| u.getType() == UnitType.Zerg_Spore_Colony)) {
								hasFoundCreepGenerator = true;
							}
						}

						if (hasFoundCreepGenerator == false) {
							isDeadlockCase = true;
						}
					}

				}
				// ��ũ�� ���, �ش� ����ġ�� �̹� �߰ų�, �̹� �ϰ��ְų�, ����ġ�� �ϴ� �ǹ� �� ����ǹ��� ���������ʰ�
				// �Ǽ����������� ������ dead lock
				else if (currentItem.metaType.isTech()) {
					TechType techType = currentItem.metaType.getTechType();
					UnitType requiredUnitType = techType.requiredUnit();

					/*
					 * System.out.println("To research " + techType.toString() +
					 * ", hasResearched " +
					 * MyBotModule.Broodwar.self().hasResearched(techType) +
					 * ", isResearching " +
					 * MyBotModule.Broodwar.self().isResearching(techType) +
					 * ", producerType " + producerType.toString() +
					 * " completedUnitCount " +
					 * MyBotModule.Broodwar.self().completedUnitCount(
					 * producerType) + " incompleteUnitCount " +
					 * MyBotModule.Broodwar.self().incompleteUnitCount(
					 * producerType));
					 */

					if (MyBotModule.Broodwar.self().hasResearched(techType)
							|| MyBotModule.Broodwar.self().isResearching(techType)) {
						isDeadlockCase = true;
					} 
					else if (MyBotModule.Broodwar.self().completedUnitCount(producerType) == 0
							&& MyBotModule.Broodwar.self().incompleteUnitCount(producerType) == 0) 
					{
						if (ConstructionManager.Instance().getConstructionQueueItemCount(producerType, null) == 0) 
						{

							// ��ũ ����ġ�� producerType�� Addon �ǹ��� ���, Addon �ǹ� �Ǽ���
							// ��� ���������� ���۵Ǳ� �������� getUnits, completedUnitCount,
							// incompleteUnitCount ���� Ȯ���� �� ����
							// producerType�� producerType �ǹ��� ���� Addon �ǹ� �Ǽ���
							// ����� ���������� Ȯ���ؾ� �Ѵ�
							if (producerType.isAddon()) {

								boolean isAddonConstructing = false;

								UnitType producerTypeOfProducerType = producerType.whatBuilds().first;

								if (producerTypeOfProducerType != UnitType.None) {

									for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
										if (unit == null)
											continue;
										if (unit.getType() != producerTypeOfProducerType) {
											continue;
										}

										// ��ǹ��� �ϼ��Ǿ��ְ�, ��ǹ��� �ش� Addon �ǹ��� �Ǽ�������
										// Ȯ���Ѵ�
										if (unit.isCompleted() && unit.isConstructing()
												&& unit.getBuildType() == producerType) {
											isAddonConstructing = true;
											break;
										}
									}
								}

								if (isAddonConstructing == false) {
									isDeadlockCase = true;
								}
							} else {
								isDeadlockCase = true;
							}
						}
					} 
					else if (requiredUnitType != UnitType.None) {
						/*
						 * std::cout + "To research " + techType.getName() +
						 * ", requiredUnitType " + requiredUnitType.getName() +
						 * " completedUnitCount " +
						 * MyBotModule.Broodwar.self().completedUnitCount(
						 * requiredUnitType) + " incompleteUnitCount " +
						 * MyBotModule.Broodwar.self().incompleteUnitCount(
						 * requiredUnitType) + std::endl;
						 */

						if (MyBotModule.Broodwar.self().completedUnitCount(requiredUnitType) == 0
								&& MyBotModule.Broodwar.self().incompleteUnitCount(requiredUnitType) == 0) {
							if (ConstructionManager.Instance().getConstructionQueueItemCount(requiredUnitType,
									null) == 0) {
								isDeadlockCase = true;
							}
						}
					}
				}
				// ���׷��̵��� ���, �ش� ���׷��̵带 �̹� �߰ų�, �̹� �ϰ��ְų�, ���׷��̵带 �ϴ� �ǹ� �� ����ǹ���
				// ���������� �ʰ� �Ǽ����������� ������ dead lock
				else if (currentItem.metaType.isUpgrade()) {
					UpgradeType upgradeType = currentItem.metaType.getUpgradeType();
					int maxLevel = MyBotModule.Broodwar.self().getMaxUpgradeLevel(upgradeType);
					int currentLevel = MyBotModule.Broodwar.self().getUpgradeLevel(upgradeType);
					UnitType requiredUnitType = upgradeType.whatsRequired();

					/*
					 * std::cout + "To upgrade " + upgradeType.getName() +
					 * ", maxLevel " + maxLevel + ", currentLevel " +
					 * currentLevel + ", isUpgrading " +
					 * MyBotModule.Broodwar.self().isUpgrading(upgradeType) +
					 * ", producerType " + producerType.getName() +
					 * " completedUnitCount " +
					 * MyBotModule.Broodwar.self().completedUnitCount(
					 * producerType) + " incompleteUnitCount " +
					 * MyBotModule.Broodwar.self().incompleteUnitCount(
					 * producerType) + ", requiredUnitType " +
					 * requiredUnitType.getName() + std::endl;
					 */

					if (currentLevel >= maxLevel || MyBotModule.Broodwar.self().isUpgrading(upgradeType)) {
						isDeadlockCase = true;
					} else if (MyBotModule.Broodwar.self().completedUnitCount(producerType) == 0
							&& MyBotModule.Broodwar.self().incompleteUnitCount(producerType) == 0) {
						if (ConstructionManager.Instance().getConstructionQueueItemCount(producerType, null) == 0) {

							// ���׷��̵��� producerType�� Addon �ǹ��� ���, Addon �ǹ� �Ǽ���
							// ���۵Ǳ� �������� getUnits, completedUnitCount,
							// incompleteUnitCount ���� Ȯ���� �� ����
							// producerType�� producerType �ǹ��� ���� Addon �ǹ� �Ǽ���
							// ���۵Ǿ��������� Ȯ���ؾ� �Ѵ�
							if (producerType.isAddon()) {

								boolean isAddonConstructing = false;

								UnitType producerTypeOfProducerType = producerType.whatBuilds().first;

								if (producerTypeOfProducerType != UnitType.None) {

									for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
										if (unit == null)
											continue;
										if (unit.getType() != producerTypeOfProducerType) {
											continue;
										}
										// ��ǹ��� �ϼ��Ǿ��ְ�, ��ǹ��� �ش� Addon �ǹ��� �Ǽ�������
										// Ȯ���Ѵ�
										if (unit.isCompleted() && unit.isConstructing()
												&& unit.getBuildType() == producerType) {
											isAddonConstructing = true;
											break;
										}
									}
								}

								if (isAddonConstructing == false) {
									isDeadlockCase = true;
								}
							} else {
								isDeadlockCase = true;
							}
						}
					} else if (requiredUnitType != UnitType.None) {
						if (MyBotModule.Broodwar.self().completedUnitCount(requiredUnitType) == 0
								&& MyBotModule.Broodwar.self().incompleteUnitCount(requiredUnitType) == 0) {
							if (ConstructionManager.Instance().getConstructionQueueItemCount(requiredUnitType,
									null) == 0) {
								isDeadlockCase = true;
							}
						}
					}
				}

				if (isDeadlockCase) {
					System.out.println(
							"Build Order Dead lock case . remove BuildOrderItem " + currentItem.metaType.getName());

					buildQueue.removeCurrentItem();
				}

			}
		}
	}
};