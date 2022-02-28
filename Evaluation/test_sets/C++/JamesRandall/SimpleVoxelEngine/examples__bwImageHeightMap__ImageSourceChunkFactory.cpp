#include "ImageSourceChunkFactory.h"
#include "Chunk.h"
#include "IChunkManager.h"
#include "GreyScaleVoxel.h"


ImageSourceChunkFactory::ImageSourceChunkFactory(std::string sourceImageFilename, bool inverseHeight, bool inverseColor, bool isBlackAndWhite) : _image(sourceImageFilename.c_str()), _inverseHeight(inverseHeight), _inverseColor(inverseColor), _isBlackAndWhite(isBlackAndWhite)
{
	
}

ImageSourceChunkFactory::~ImageSourceChunkFactory()
{
}

std::shared_ptr<IChunk> ImageSourceChunkFactory::construct(const IChunkManager* chunkManager, unsigned chunkX, unsigned chunkY, unsigned chunkZ)
{
	std::vector<std::shared_ptr<IVoxel>> voxels;
	unsigned rValues[IChunk::Width][IChunk::Height];
	unsigned gValues[IChunk::Width][IChunk::Height];
	unsigned bValues[IChunk::Width][IChunk::Height];

	for (unsigned z = 0; z < IChunk::Depth; z++)
	{
		unsigned worldZ = chunkZ * IChunk::Depth + z;
		for (unsigned x = 0; x < IChunk::Width; x++)
		{
			unsigned worldX = chunkX * IChunk::Width + x;
			if (worldX < static_cast<unsigned>(_image.width()) && worldZ < static_cast<unsigned>(_image.height()))
			{
				unsigned char r = _image(worldX, worldZ, 0, 0);
				unsigned char g = _image(worldX, worldZ, 0, 1);
				unsigned char b = _image(worldX, worldZ, 0, 2);
				rValues[x][z] = r;
				gValues[x][z] = g;
				bValues[x][z] = b;
			}
		}
	}

	for (unsigned z = 0; z < IChunk::Depth; z++)
	{
		for (unsigned y = 0; y < IChunk::Height; y++)
		{
			unsigned worldY = chunkY * IChunk::Height + y;

			for (unsigned x = 0; x < IChunk::Width; x++)
			{
				if (_isBlackAndWhite)
				{
					unsigned char r = static_cast<unsigned char>(rValues[x][z]);
					float fractionalR = static_cast<float>(r) / 255.0f;
					if (_inverseHeight) fractionalR = 1.0f - fractionalR;
					if (_inverseColor) r = 255 - r;
					unsigned colorWorldY = static_cast<unsigned>((chunkManager->getHeight() * IChunk::Height) * fractionalR);
					if (colorWorldY > worldY)
					{
						voxels.push_back(std::make_shared<GreyScaleVoxel>(r));
					}
					else
					{
						voxels.push_back(nullptr);
					}
				}
				else
				{
					float luminance = 0.2126f * rValues[x][z] / 255.0f + 0.7152f * gValues[x][z] / 255.0f + 0.0722 * bValues[x][z] / 255.0f;
					if (_inverseHeight) luminance = 1.0f - luminance;
					unsigned colorWorldY = static_cast<unsigned>((chunkManager->getHeight() * IChunk::Height) * luminance);
					if (colorWorldY > worldY)
					{
						voxels.push_back(std::make_shared<RgbVoxel>(rValues[x][z], gValues[x][z], bValues[x][z]));
					}
					else
					{
						voxels.push_back(nullptr);
					}
				}
			}
		}
	}

	std::shared_ptr<IChunk> chunk = std::make_shared<Chunk>(voxels);
	return chunk;
}
