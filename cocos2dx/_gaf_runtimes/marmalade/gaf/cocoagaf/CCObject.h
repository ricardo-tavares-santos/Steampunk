#pragma once

#ifndef __GAF_COCOA_OBJECT_H__
#define __GAF_COCOA_OBJECT_H__

#include "CCCocos2dx.h"

namespace GAF
{
	class CCObject
	{
	public:
		CCObject();
		virtual ~CCObject();
		void retain();
		void release();
		CCObject* autorelease();
		static void process_autoreleased_objects();
	private:
		unsigned int _retainCount;
	};
}

#endif // __GAF_COCOA_OBJECT_H__
