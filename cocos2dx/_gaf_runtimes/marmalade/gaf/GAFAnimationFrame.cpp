#include "GAFAnimationFrame.h"
#include "GAFSubobjectState.h"
#include "GAFTextureAtlas.h"
#include "cocoagaf/CCArray.h"
#include "cocoagaf/CCDictionary.h"



namespace GAF
{
	
GAFAnimationFrame::GAFAnimationFrame()
:
_initialInstructions(NULL),
_objectsStates(NULL)
{
	
}

GAFAnimationFrame::~GAFAnimationFrame()
{
	CC_SAFE_RELEASE(_initialInstructions);
	CC_SAFE_RELEASE(_objectsStates);
}

CCArray * GAFAnimationFrame::objectStates()
{
	return _objectsStates;
}

void GAFAnimationFrame::setObjectStates(CCArray * states)
{
	CC_SAFE_RELEASE(_objectsStates);
	_objectsStates = states;
	CC_SAFE_RETAIN(_objectsStates);
}

}
