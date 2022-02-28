/*
  ==============================================================================

    pMixDefaultColours.h
    Author:  Oliver Larkin

  ==============================================================================
*/

#pragma once

#include "JuceHeader.h"

class PMixDefaultColours
{
public:
  PMixDefaultColours()
  : idx(0)
  {
    mColours.add(Colours::red);
    mColours.add(Colours::green);
    mColours.add(Colours::blue);
    mColours.add(Colours::yellow);
    mColours.add(Colours::magenta);
    mColours.add(Colours::skyblue);
  }
  
  ~PMixDefaultColours()
  {
    
  }
  
  Colour getNextColour()
  {
    Colour nextColour = mColours[idx++];
    
    if (idx > mColours.size())
      idx = 0;
    
    return nextColour;
  }
  
private:
  Array<Colour> mColours;
  uint32 idx;
};




