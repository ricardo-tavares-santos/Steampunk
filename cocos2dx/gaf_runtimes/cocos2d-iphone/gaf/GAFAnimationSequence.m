////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
//  GAFAnimationSequence.m
//  AnimationPlayer
//
//  Created by Gregory Maksyuk on 4/8/13.
//  Copyright 2013 Catalyst Apps. All rights reserved.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Imports

#import "GAFAnimationSequence.h"

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Constants

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Private interface

@interface GAFAnimationSequence ()

@property (nonatomic, copy  ) NSString *name;
@property (nonatomic, assign) NSRange framesRange;

@end

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Implementation

@implementation GAFAnimationSequence

#pragma mark -
#pragma mark Properties

#pragma mark -
#pragma mark Initialization & Release

- (id)initWithName:(NSString *)aName framesRange:(NSRange)aFramesRange
{
    self = [super init];
    if (nil != self)
    {
        self.name = aName;
        self.framesRange = aFramesRange;
    }
    return self;
}

- (void)dealloc
{
    [_name release];
    [super dealloc];
}

#pragma mark -
#pragma mark Public methods

#pragma mark -
#pragma mark Private methods

@end