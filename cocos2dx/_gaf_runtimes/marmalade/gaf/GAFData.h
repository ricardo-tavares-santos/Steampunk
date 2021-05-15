#pragma once

#ifndef __GAF_DATA_H__
#define __GAF_DATA_H__

#include "cocoagaf/CCObject.h"


namespace GAF
{

class GAFData : public CCObject
{
public:
	inline GAFData()
	:
	size(0),
	ptr(0),
	delete_data(false)
	{
		
	}
	
	inline GAFData(unsigned char * _ptr, int _size, bool _delete_data = false)
	:
	ptr(_ptr),
	size(_size),
	delete_data(_delete_data)
	{
		
	}
	~GAFData()
	{
		if (delete_data && ptr)
		{
			delete [] ptr;
		}
	}
	
	inline unsigned char * getBytes() const
	{
		return ptr;
	}
	inline unsigned long getSize() const
	{
		return size;
	}

	unsigned long size;
	unsigned char * ptr;
	bool delete_data;
}; // GAFData
	
}  // namespace GAF


#endif // __GAF_DATA_H__
