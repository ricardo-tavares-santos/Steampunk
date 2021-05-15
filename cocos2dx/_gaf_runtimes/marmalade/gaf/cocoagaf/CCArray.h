#pragma once

#ifndef __GAF_COCOA_CCARRAY_H__
#define __GAF_COCOA_CCARRAY_H__

#include "CCObject.h"
#include <vector>

namespace GAF
{
	class CCObject;
	
	class CCArray : public CCObject
	{
	public:
		
		static CCArray* create();
		static CCArray* createWithCapacity(int size);
		
		
		inline unsigned int count() const
		{
			return _vect.size();
		}
		
		CCObject* objectAtIndex(unsigned int index)
		{
			return _vect[index];
		}
		
		void addObject(CCObject* object)
		{
			object->retain();
			_vect.push_back(object);
		}
		
		void removeAllObjects();
		
	private:
		std::vector<CCObject*> _vect;
	};
}

#endif // __GAF_COCOA_CCARRAY_H__
