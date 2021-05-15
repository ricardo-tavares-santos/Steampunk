#include "CCObject.h"
#include "IwDebug.h"
#include <vector>

namespace GAF
{

CCObject::CCObject()
	:
_retainCount(1)
{
	
}
	
CCObject::~CCObject()
{
	
}
	
void CCObject::retain()
{
	IwAssert(_retainCount > 0, "reference count should greater than 0");	
    ++_retainCount;
}
	
void CCObject::release()
{	
    IwAssert(_retainCount > 0, "reference count should greater than 0");
    --_retainCount;	
    if (0 == _retainCount)
    {
        delete this;
    }
}

// GAF on marmalade use simple minimalistic autorelease pool emulation
// we do not allow nested autoreleased pools
	
static std::vector<CCObject *> _autoreleasedPool;
	
CCObject* CCObject::autorelease()
{
	_autoreleasedPool.push_back(this);
	return this;
}
	
void CCObject::process_autoreleased_objects()
{
}
	

}

