#pragma once

#include "Common.h"
#include "MetaType.h"

namespace MyBot
{
	/// ���� ���
	struct BuildOrderItem
	{
		MetaType			metaType;		///< the thing we want to 'build'
		int					priority;		///< the priority at which to place it in the queue
		bool				blocking;		///< whether or not we block further items
		BWAPI::TilePosition seedLocation;	///< �Ǽ� ��ġ
		int					producerID;		///< producer unitID (�ǹ�ID, ����ID)

		/// �Ǽ���ġ �ʾ� ���� ��å
		/// ���� ���� ���, ��� �� �� �߰�
		enum SeedPositionStrategy { 
			MainBaseLocation,			///< �Ʊ� ���̽�
			MainBaseBackYard,			///< �Ʊ� ���̽� ����
			FirstChokePoint,			///< �Ʊ� ù��° ���
			FirstExpansionLocation,		///< �Ʊ� ù��° �ո���
			SecondChokePoint,			///< �Ʊ� �ι�° ���
			SecondExpansionLocation,	///< �Ʊ� �ι�° �ո���
			SeedPositionSpecified		///< ���� ���� ��ġ
		};
		SeedPositionStrategy		seedLocationStrategy;	///< �Ǽ���ġ �ʾ� ���� ��å

		/// �Ǽ� ��ġ�� SeedPositionStrategy::MainBaseLocation �� ���� ã�´�
		/// @param _metaType : ���� ��� Ÿ��
		/// @param _priority : 0 = ���� ���� �켱����. ���ڰ� Ŭ���� �� ���� �켱����. ť�� �ִ� �����۵� �߿��� ���� ���� �켱������ ������ (�켱������ ������ ���� ť�� ���� ��)�� ���� ���� �����. 
		/// @param _blocking : true = ���� �̰��� ������ �� ������, �̰� ���� �������������� ��ٸ�.  false = ���� �̰��� ������ �� �� ������ ������ ���� ���� ����.
		/// @param _producerID : producerID �� �����ϸ� �ش� unit �� ���带 �����ϰ� ��
		BuildOrderItem(MetaType _metaType, int _priority = 0, bool _blocking = true, int _producerID = -1)
			: metaType(_metaType)
			, priority(_priority)
			, blocking(_blocking)
			, producerID(_producerID)
			, seedLocation(BWAPI::TilePositions::None)
			, seedLocationStrategy(SeedPositionStrategy::MainBaseLocation)
		{
		}

		/// �Ǽ� ��ġ�� seedLocation �������� ã�´�
		BuildOrderItem(MetaType _metaType, BWAPI::TilePosition _seedLocation, int _priority = 0, bool _blocking = true, int _producerID = -1)
			: metaType(_metaType)
			, priority(_priority)
			, blocking(_blocking)
			, producerID(_producerID)
			, seedLocation(_seedLocation)
			, seedLocationStrategy(SeedPositionStrategy::SeedPositionSpecified)
		{
		}

		/// �Ǽ� ��ġ�� seedPositionStrategy �� �̿��ؼ� ã�´�. ��ã�� ���, �ٸ� SeedPositionStrategy �� ��� ã�´�
		BuildOrderItem(MetaType _metaType, SeedPositionStrategy _SeedPositionStrategy, int _priority = 0, bool _blocking = true, int _producerID = -1)
			: metaType(_metaType)
			, priority(_priority)
			, blocking(_blocking)
			, producerID(_producerID)
			, seedLocation(BWAPI::TilePositions::None)
			, seedLocationStrategy(_SeedPositionStrategy)
		{
		}



		bool operator<(const BuildOrderItem &x) const
		{
			return priority < x.priority;
		}
	};

	/// ���� ���� ��� �ڷᱸ�� class
	class BuildOrderQueue
	{
		/// BuildOrderItem ���� Double Ended Queue �ڷᱸ���� �����մϴ�
		/// lowest priority �� BuildOrderItem�� front ��, highest priority �� BuildOrderItem �� back �� ��ġ�ϰ� �մϴ�
		std::deque< BuildOrderItem >			queue;

		int lowestPriority;
		int highestPriority;
		int defaultPrioritySpacing;

		/// iteration �� �ϱ� ���� ����
		/// highest priority �� BuildOrderItem ���κ��� ��� skip �ߴ°�. 
		int numSkippedItems;

	public:

		BuildOrderQueue();

		/// queues something with a given priority
		void queueItem(BuildOrderItem b);

		/// ��������� ���� ���� �켱������ buildQueue �� �߰��Ѵ�. blocking (�ٸ� ���� �������� �ʰ�, �̰��� ���� �����ϰ� �� ������ ��ٸ��� ���) �⺻���� true �̴�
		void queueAsHighestPriority(MetaType				_metaType, bool blocking = true, int _producerID = -1);		
		void queueAsHighestPriority(MetaType                _metaType, BuildOrderItem::SeedPositionStrategy _seedPositionStrategy = BuildOrderItem::SeedPositionStrategy::MainBaseLocation, bool blocking = true);
		void queueAsHighestPriority(MetaType                _metaType, BWAPI::TilePosition _seedPosition, bool blocking = true);
		void queueAsHighestPriority(BWAPI::UnitType         _unitType, BuildOrderItem::SeedPositionStrategy _seedPositionStrategy = BuildOrderItem::SeedPositionStrategy::MainBaseLocation, bool blocking = true);
		void queueAsHighestPriority(BWAPI::UnitType         _unitType, BWAPI::TilePosition _seedPosition, bool blocking = true);
		void queueAsHighestPriority(BWAPI::TechType         _techType, bool blocking = true, int _producerID = -1);
		void queueAsHighestPriority(BWAPI::UpgradeType      _upgradeType, bool blocking = true, int _producerID = -1);

		/// ��������� ���� ���� �켱������ buildQueue �� �߰��Ѵ�. blocking (�ٸ� ���� �������� �ʰ�, �̰��� ���� �����ϰ� �� ������ ��ٸ��� ���) �⺻���� true �̴�
		void queueAsLowestPriority(MetaType				   _metaType, bool blocking = true, int _producerID = -1);
		void queueAsLowestPriority(MetaType                _metaType, BuildOrderItem::SeedPositionStrategy _seedPositionStrategy = BuildOrderItem::SeedPositionStrategy::MainBaseLocation, bool blocking = true);
		void queueAsLowestPriority(MetaType                _metaType, BWAPI::TilePosition _seedPosition, bool blocking = true);
		void queueAsLowestPriority(BWAPI::UnitType         _unitType, BuildOrderItem::SeedPositionStrategy _seedPositionStrategy = BuildOrderItem::SeedPositionStrategy::MainBaseLocation, bool blocking = true);
		void queueAsLowestPriority(BWAPI::UnitType         _unitType, BWAPI::TilePosition _seedPosition, bool blocking = true);
		void queueAsLowestPriority(BWAPI::TechType         _techType, bool blocking = true, int _producerID = -1);
		void queueAsLowestPriority(BWAPI::UpgradeType      _upgradeType, bool blocking = true, int _producerID = -1);

		void clearAll();											///< clears the entire build order queue
		
		size_t size();												///< returns the size of the queue
		bool isEmpty();

		BuildOrderItem getHighestPriorityItem();					///< returns the highest priority item
		void removeHighestPriorityItem();							///< removes the highest priority item

		bool canSkipCurrentItem();
		void skipCurrentItem();										///< increments skippedItems
		void removeCurrentItem();									///< skippedItems ������ item �� �����մϴ�
		BuildOrderItem getNextItem();								///< returns the highest priority item

		/// BuildOrderQueue�� �ش� type �� Item �� �� �� �����ϴ��� �����Ѵ�. queryTilePosition �� �Է��� ���, �ǹ��� ���ؼ� �߰� Ž���Ѵ�
		int getItemCount(MetaType                _metaType, BWAPI::TilePosition queryTilePosition = BWAPI::TilePositions::None);
		/// BuildOrderQueue�� �ش� type �� Item �� �� �� �����ϴ��� �����Ѵ�. queryTilePosition �� �Է��� ���, �ǹ��� ���ؼ� �߰� Ž���Ѵ�
		int getItemCount(BWAPI::UnitType         _unitType, BWAPI::TilePosition queryTilePosition = BWAPI::TilePositions::None);
		int getItemCount(BWAPI::TechType         _techType);
		int getItemCount(BWAPI::UpgradeType      _upgradeType);

		int getHighestPriorityValue();								///< returns the highest priority value


		BuildOrderItem operator [] (int i);							///< overload the bracket operator for ease of use

		std::deque< BuildOrderItem > *			getQueue();
	};
}