#pragma once

#ifndef __GAF_COCOA_CCDICTIONARY_H__
#define __GAF_COCOA_CCDICTIONARY_H__

#include <string>
#include <map>
#include "CCObject.h"



namespace GAF
{
	class CCDictionary : public CCObject, public std::map<std::string, GAF::CCObject*>
	{
	public:
		
		static CCDictionary* create();
		
		CCObject * objectForKey(const std::string& key);
		
		inline unsigned int count() const
		{
			return this->size();
		}
		
		void setObject(CCObject * object, const char * key);
		
		void removeAllObjects();
	};
	
	typedef std::map<std::string, GAF::CCObject*>::iterator CCDictionaryIt;
	
}

#endif // __GAF_COCOA_OBJECT_H__
