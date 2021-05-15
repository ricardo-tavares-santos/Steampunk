// Marmalade headers
#include "s3e.h"
#include "Iw2D.h"
#include "IwGx.h"

#include "GAFData.h"

int main()
{
	// Initialise Mamrlade graphics system and Iw2D module
	IwGxInit();
    Iw2DInit();

	// Set the default background clear colour
	IwGxSetColClear(0x40, 0x40, 0x40, 0);

	// Main Game Loop
	while (!s3eDeviceCheckQuitRequest())
	{
		// Update keyboard system
		s3eKeyboardUpdate();
		if (s3eKeyboardGetState(s3eKeyAbsBSK) & S3E_KEY_STATE_DOWN)	// Back key is used to exit on some platforms
			break;

		// Update pointer system
		s3ePointerUpdate();

		// Clear the screen
		IwGxClear(IW_GX_COLOUR_BUFFER_F | IW_GX_DEPTH_BUFFER_F);

		// Update the game

		// Render the games view
	
		// Show the surface
		Iw2DSurfaceShow();

		// Yield to the opearting system
		s3eDeviceYield(0);

	}

	// Shut down Marmalade graphics system and the Iw2D module
	Iw2DTerminate();
	IwGxTerminate();

    return 0;
}

