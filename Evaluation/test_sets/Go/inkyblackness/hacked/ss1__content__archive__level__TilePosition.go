package level

// TilePosition contains the coordinates of a tile in a level.
type TilePosition struct {
	X byte
	Y byte
}

// FinePosition contains the coordinates of an object within a tile.
type FinePosition struct {
	X byte
	Y byte
}

// SlopeFactorFor returns the factor of how much the slope of given tile type would apply at the fine position.
func (pos FinePosition) SlopeFactorFor(tileType TileType) float32 {
	southToNorth := float32(pos.Y) / 255
	westToEast := float32(pos.X) / 255
	swneDiag := func(northWest, southEast float32) float32 {
		if pos.X < pos.Y {
			return northWest
		}
		return southEast
	}
	nwseDiag := func(southWest, northEast float32) float32 {
		if (255 - pos.X) < pos.Y {
			return northEast
		}
		return southWest
	}

	switch tileType {
	case TileTypeSlopeSouthToNorth:
		return southToNorth
	case TileTypeSlopeWestToEast:
		return westToEast
	case TileTypeSlopeNorthToSouth:
		return 1 - southToNorth
	case TileTypeSlopeEastToWest:
		return 1 - westToEast

	case TileTypeValleySouthEastToNorthWest:
		return nwseDiag(1-westToEast, southToNorth)
	case TileTypeValleySouthWestToNorthEast:
		return swneDiag(southToNorth, westToEast)
	case TileTypeValleyNorthWestToSouthEast:
		return nwseDiag(1-southToNorth, westToEast)
	case TileTypeValleyNorthEastToSouthWest:
		return swneDiag(1-westToEast, 1-southToNorth)

	case TileTypeRidgeNorthWestToSouthEast:
		return nwseDiag(westToEast, 1-southToNorth)
	case TileTypeRidgeNorthEastToSouthWest:
		return swneDiag(1-southToNorth, 1-westToEast)
	case TileTypeRidgeSouthEastToNorthWest:
		return nwseDiag(southToNorth, 1-westToEast)
	case TileTypeRidgeSouthWestToNorthEast:
		return swneDiag(westToEast, southToNorth)
	}

	return 0.0
}
