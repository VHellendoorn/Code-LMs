/*
AI Lib
Copyright (C) 2014 Eric Laukien

This software is provided 'as-is', without any express or implied
warranty.  In no event will the authors be held liable for any damages
arising from the use of this software.

Permission is granted to anyone to use this software for any purpose,
including commercial applications, and to alter it and redistribute it
freely, subject to the following restrictions:

1. The origin of this software must not be misrepresented; you must not
claim that you wrote the original software. If you use this software
in a product, an acknowledgment in the product documentation would be
appreciated but is not required.
2. Altered source versions must be plainly marked as such, and must not be
misrepresented as being the original software.
3. This notice may not be removed or altered from any source distribution.
*/

#pragma once

#include <vector>
#include <numeric>
#include <random>

namespace deep {
	class DSOM {
	public:
		struct SOMNode {
			std::vector<float> _weights;

			float differenceSquared(const std::vector<float> &input) const {
				float sum = 0.0f;

				for (size_t i = 0; i < _weights.size(); i++) {
					float delta = input[i] - _weights[i];

					sum += delta * delta;
				}

				return sum;
			}
		};

		struct SOMCoords {
			std::vector<int> _coords;
		};

		struct SOMCoordsReal {
			std::vector<float> _coords;
		};

		float sigmoid(float x) {
			return 1.0f / (1.0f + std::exp(-x));
		}

	private:
		std::vector<SOMNode> _nodes;

		size_t _numInputs;
		size_t _dimensions;
		size_t _dimensionSize;

	public:
		float _neighborhoodRadius; // Neighborhood radius as a fraction of the size of the map
		float _gaussianScalar; // Scales the falloff
		float _alpha; // Learning rate
		float _radiusDecay;

		DSOM();

		void createRandom(size_t numInputs, size_t dimensions, size_t dimensionSize, float minWeight, float maxWeight, std::mt19937 &generator);

		SOMNode &getNode(const SOMCoords &coords);

		SOMNode &getNode(size_t index) {
			return _nodes[index];
		}

		SOMCoords getBestMatchingUnit(const std::vector<float> &input);
		SOMCoordsReal getBestMatchingUnitReal(const std::vector<float> &input, float closenessFactor);

		void updateNeighborhood(const SOMCoords &centerCoords, const std::vector<float> &target);

		size_t getNumInputs() const {
			return _numInputs;
		}

		size_t getDimensions() const {
			return _dimensions;
		}

		size_t getDimensionSize() const {
			return _dimensionSize;
		}

		size_t getNumNodes() const {
			return _nodes.size();
		}
	};
}