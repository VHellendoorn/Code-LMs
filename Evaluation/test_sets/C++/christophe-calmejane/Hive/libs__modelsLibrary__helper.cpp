/*
* Copyright (C) 2017-2021, Emilien Vallot, Christophe Calmejane and other contributors

* This file is part of Hive.

* Hive is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.

* Hive is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.

* You should have received a copy of the GNU Lesser General Public License
* along with Hive.  If not, see <http://www.gnu.org/licenses/>.
*/


#include "hive/modelsLibrary/helper.hpp"

#include <la/avdecc/utils.hpp>

#include <cctype>

namespace hive
{
namespace modelsLibrary
{
namespace helper
{
QString uniqueIdentifierToString(la::avdecc::UniqueIdentifier const& identifier)
{
	return toHexQString(identifier.getValue(), true, true);
}

QString configurationName(la::avdecc::controller::ControlledEntity const* const controlledEntity, la::avdecc::controller::model::ConfigurationNode const& node) noexcept
{
	return objectName(controlledEntity, node);
}

QString localizedString(la::avdecc::controller::ControlledEntity const& controlledEntity, la::avdecc::entity::model::LocalizedStringReference const stringReference) noexcept
{
	auto const& localizedName = controlledEntity.getLocalizedString(stringReference);

	if (localizedName.empty())
	{
		return "(No Localization)";
	}
	return localizedName.data();
}

QString entityName(la::avdecc::controller::ControlledEntity const& controlledEntity) noexcept
{
	try
	{
		auto const& entity = controlledEntity.getEntity();

		if (entity.getEntityCapabilities().test(la::avdecc::entity::EntityCapability::AemSupported))
		{
			return controlledEntity.getEntityNode().dynamicModel->entityName.data();
		}
	}
	catch (la::avdecc::controller::ControlledEntity::Exception const&)
	{
		// Ignore exception
	}
	catch (...)
	{
		// Uncaught exception
		AVDECC_ASSERT(false, "Uncaught exception");
	}
	return {};
}

QString smartEntityName(la::avdecc::controller::ControlledEntity const& controlledEntity) noexcept
{
	QString name;

	name = entityName(controlledEntity);

	if (name.isEmpty())
		name = uniqueIdentifierToString(controlledEntity.getEntity().getEntityID());

	return name;
}

QString groupName(la::avdecc::controller::ControlledEntity const& controlledEntity) noexcept
{
	try
	{
		auto const& entity = controlledEntity.getEntity();

		if (entity.getEntityCapabilities().test(la::avdecc::entity::EntityCapability::AemSupported))
		{
			return controlledEntity.getEntityNode().dynamicModel->groupName.data();
		}
	}
	catch (la::avdecc::controller::ControlledEntity::Exception const&)
	{
		// Ignore exception
	}
	catch (...)
	{
		// Uncaught exception
		AVDECC_ASSERT(false, "Uncaught exception");
	}
	return {};
}

QString outputStreamName(la::avdecc::controller::ControlledEntity const& controlledEntity, la::avdecc::entity::model::StreamIndex const streamIndex) noexcept
{
	try
	{
		auto const& entityNode = controlledEntity.getEntityNode();
		auto const& streamNode = controlledEntity.getStreamOutputNode(entityNode.dynamicModel->currentConfiguration, streamIndex);
		return objectName(&controlledEntity, streamNode);
	}
	catch (la::avdecc::controller::ControlledEntity::Exception const&)
	{
		// Ignore exception
	}
	catch (...)
	{
		// Uncaught exception
		AVDECC_ASSERT(false, "Uncaught exception");
	}
	return {};
}

QString inputStreamName(la::avdecc::controller::ControlledEntity const& controlledEntity, la::avdecc::entity::model::StreamIndex const streamIndex) noexcept
{
	try
	{
		auto const& entityNode = controlledEntity.getEntityNode();
		auto const& streamNode = controlledEntity.getStreamInputNode(entityNode.dynamicModel->currentConfiguration, streamIndex);
		return objectName(&controlledEntity, streamNode);
	}
	catch (la::avdecc::controller::ControlledEntity::Exception const&)
	{
		// Ignore exception
	}
	catch (...)
	{
		// Uncaught exception
		AVDECC_ASSERT(false, "Uncaught exception");
	}
	return {};
}

QString redundantOutputName(la::avdecc::controller::model::VirtualIndex const redundantIndex) noexcept
{
	return QString{ "Redundant Stream Output %1" }.arg(QString::number(redundantIndex));
}

QString redundantInputName(la::avdecc::controller::model::VirtualIndex const redundantIndex) noexcept
{
	return QString{ "Redundant Stream Input %1" }.arg(QString::number(redundantIndex));
}

QString toUpperCamelCase(std::string const& text) noexcept
{
#pragma message("TODO: Use a regex, if possible")
	auto output = std::string{};

	auto shouldUpperCase = true;
	for (auto const c : text)
	{
		if (c == '_')
		{
			output.push_back(' ');
			shouldUpperCase = true;
		}
		else if (shouldUpperCase)
		{
			output.push_back(std::toupper(c));
			shouldUpperCase = false;
		}
		else
		{
			output.push_back(std::tolower(c));
		}
	}

	return QString::fromStdString(output);
}

QString getVendorName(la::avdecc::UniqueIdentifier const entityID) noexcept
{
	static auto s_oui24ToName = std::unordered_map<std::uint32_t, QString>{};
	static auto s_oui36ToName = std::unordered_map<std::uint64_t, QString>{};

	// Map is empty, load it
	if (s_oui24ToName.empty())
	{
		// Right now statically build the map, later use a resource file generated from https://standards.ieee.org/develop/regauth/oui/oui.csv
		s_oui24ToName.emplace(std::make_pair(0x001B92, "l-acoustics"));
		s_oui24ToName.emplace(std::make_pair(0x001CF7, "AudioScience"));
		s_oui24ToName.emplace(std::make_pair(0xB4994C, "Texas Instruments"));
		s_oui24ToName.emplace(std::make_pair(0x3CC0C6, "d&b audiotechnik GmbH"));
		s_oui24ToName.emplace(std::make_pair(0x001CAB, "Meyer Sound Laboratories, Inc."));
		s_oui24ToName.emplace(std::make_pair(0x0C4DE9, "Apple"));
		s_oui24ToName.emplace(std::make_pair(0x0090E5, "TEKNEMA, INC."));
		s_oui24ToName.emplace(std::make_pair(0x0001F2, "Mark of the Unicorn, Inc."));
		s_oui24ToName.emplace(std::make_pair(0xD0699E, "LUMINEX Lighting Control Equipment"));
		s_oui24ToName.emplace(std::make_pair(0xCC46D6, "Cisco Systems, Inc"));
		s_oui24ToName.emplace(std::make_pair(0x58AC78, "Cisco Systems, Inc"));
		s_oui24ToName.emplace(std::make_pair(0x00107B, "Cisco Systems, Inc"));
		s_oui24ToName.emplace(std::make_pair(0x00906D, "Cisco Systems, Inc"));
		s_oui24ToName.emplace(std::make_pair(0x0090BF, "Cisco Systems, Inc"));
		s_oui24ToName.emplace(std::make_pair(0x005080, "Cisco Systems, Inc"));
		s_oui24ToName.emplace(std::make_pair(0xF4CFE2, "Cisco Systems, Inc"));
		s_oui24ToName.emplace(std::make_pair(0x501CBF, "Cisco Systems, Inc"));
		s_oui24ToName.emplace(std::make_pair(0x88F031, "Cisco Systems, Inc"));
		s_oui24ToName.emplace(std::make_pair(0x508789, "Cisco Systems, Inc")); // Too many cisco OUI-24 to manually add them
		s_oui24ToName.emplace(std::make_pair(0x00A07E, "AVID TECHNOLOGY, INC."));
		s_oui24ToName.emplace(std::make_pair(0xD88466, "Extreme Networks, Inc."));
		s_oui24ToName.emplace(std::make_pair(0x000496, "Extreme Networks, Inc."));
		s_oui24ToName.emplace(std::make_pair(0xB85001, "Extreme Networks, Inc."));
		s_oui24ToName.emplace(std::make_pair(0x5C0E8B, "Extreme Networks, Inc."));
		s_oui24ToName.emplace(std::make_pair(0xB4C799, "Extreme Networks, Inc."));
		s_oui24ToName.emplace(std::make_pair(0x7467F7, "Extreme Networks, Inc."));
		s_oui24ToName.emplace(std::make_pair(0x00E02B, "Extreme Networks, Inc."));
		s_oui24ToName.emplace(std::make_pair(0x949B2C, "Extreme Networks, Inc."));
		s_oui24ToName.emplace(std::make_pair(0xA4EA8E, "Extreme Networks, Inc."));
		s_oui24ToName.emplace(std::make_pair(0xFC0A81, "Extreme Networks, Inc."));
		s_oui24ToName.emplace(std::make_pair(0xB42D56, "Extreme Networks, Inc."));
		s_oui24ToName.emplace(std::make_pair(0x000130, "Extreme Networks, Inc."));
		s_oui24ToName.emplace(std::make_pair(0xF46E95, "Extreme Networks, Inc."));
		s_oui24ToName.emplace(std::make_pair(0x784501, "Biamp Systems"));
	}

	// First search in OUI-24
	{
		auto const nameIt = s_oui24ToName.find(entityID.getVendorID<std::uint32_t>());
		if (nameIt != s_oui24ToName.end())
		{
			return nameIt->second;
		}
	}

	// Then search in OUI-36
	{
		auto const nameIt = s_oui36ToName.find(entityID.getVendorID<std::uint64_t>());
		if (nameIt != s_oui36ToName.end())
		{
			return nameIt->second;
		}
	}

	// If not found, convert to hex string
	return toHexQString(entityID.getVendorID<std::uint32_t>(), true, true);
}

} // namespace helper
} // namespace modelsLibrary
} // namespace hive
