#pragma once

#ifndef __GAF_COCOS_2DX_H__
#define __GAF_COCOS_2DX_H__

#define CC_SAFE_DELETE(p)            do { if(p) { delete (p); (p) = 0; } } while(0)
#define CC_SAFE_RELEASE(p)           do { if(p) { (p)->release(); } } while(0)
#define CC_SAFE_RELEASE_NULL(p)      do { if(p) { (p)->release(); (p) = 0; } } while(0)
#define CC_SAFE_RETAIN(p)            do { if(p) { (p)->retain(); } } while(0)

#include "IwDebug.h"

#define CCLOGERROR(format, ...) 
#define CCLOGWARN(format, ...) 
#define CCLog(str)

#define CCAssert(cond, msg)

#define CCDICT_FOREACH(___dict_, pElement) for(CCDictionaryIt pElement = (___dict_) ->begin(); pElement != (___dict_) ->end(); ++pElement)

#endif // __GAF_COCOS_2DX_H__
