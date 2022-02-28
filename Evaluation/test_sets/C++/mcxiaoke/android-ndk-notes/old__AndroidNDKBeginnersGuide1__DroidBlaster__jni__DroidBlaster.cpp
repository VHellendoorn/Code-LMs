#include "DroidBlaster.hpp"
#include "Log.hpp"

namespace dbs {
    DroidBlaster::DroidBlaster(packt::Context* pContext) :
        mGraphicsService(pContext->mGraphicsService),
        mInputService(pContext->mInputService),
        mSoundService(pContext->mSoundService),
        mTimeService(pContext->mTimeService),
        mBackground(pContext), mShip(pContext), mAsteroids(),
        mStartSound(mSoundService->registerSound(
                            "/sdcard/droidblaster/start.pcm")) {
        packt::Log::info("Creating DroidBlaster");

        // Creates asteroids.
        for (int i = 0; i < 16; ++i) {
            Asteroid::ptr lAsteroid(new Asteroid(pContext));
            mAsteroids.push_back(lAsteroid);
        }
    }

    DroidBlaster::~DroidBlaster()
    {}

    packt::status DroidBlaster::onActivate() {
        packt::Log::info("Activating DroidBlaster");

        // Starts services.
        if (mGraphicsService->start() != packt::STATUS_OK) {
            return packt::STATUS_KO;
        }
        if (mInputService->start() != packt::STATUS_OK) {
            return packt::STATUS_KO;
        }
        if (mSoundService->start() != packt::STATUS_OK) {
            return packt::STATUS_KO;
        }

        // Starts background music.
        mSoundService->playBGM("/sdcard/droidblaster/bgm.mp3");
        mSoundService->playSound(mStartSound);
        // Initializes game objects.
        mBackground.spawn();
        mShip.spawn();

        Asteroid::vec_it iAsteroid = mAsteroids.begin();
        for (; iAsteroid < mAsteroids.end() ; ++iAsteroid) {
            (*iAsteroid)->spawn();
        }

        mTimeService->reset();
        return packt::STATUS_OK;
    }

    void DroidBlaster::onDeactivate() {
        packt::Log::info("Deactivating DroidBlaster");
        mGraphicsService->stop();
        mInputService->stop();
        mSoundService->stop();
    }

    packt::status DroidBlaster::onStep() {
        mTimeService->update();

        // Updates entities.
        mBackground.update();
        mShip.update();
        Asteroid::vec_it iAsteroid = mAsteroids.begin();
        for (; iAsteroid < mAsteroids.end(); ++iAsteroid) {
            (*iAsteroid)->update();
        }

        // Updates services.
        if (mGraphicsService->update() != packt::STATUS_OK) {
            return packt::STATUS_KO;
        }
        if (mInputService->update() != packt::STATUS_OK) {
            return packt::STATUS_KO;
        }
        return packt::STATUS_OK;
    }

    void DroidBlaster::onStart() {
        packt::Log::info("onStart");
    }

    void DroidBlaster::onResume() {
        packt::Log::info("onResume");
    }

    void DroidBlaster::onPause() {
        packt::Log::info("onPause");
    }

    void DroidBlaster::onStop() {
        packt::Log::info("onStop");
    }

    void DroidBlaster::onDestroy() {
        packt::Log::info("onDestroy");
    }

    void DroidBlaster::onSaveState(void** pData, size_t* pSize) {
        packt::Log::info("onSaveInstanceState");
    }

    void DroidBlaster::onConfigurationChanged() {
        packt::Log::info("onConfigurationChanged");
    }

    void DroidBlaster::onLowMemory() {
        packt::Log::info("onLowMemory");
    }

    void DroidBlaster::onCreateWindow() {
        packt::Log::info("onCreateWindow");
    }

    void DroidBlaster::onDestroyWindow() {
        packt::Log::info("onDestroyWindow");
    }

    void DroidBlaster::onGainFocus() {
        packt::Log::info("onGainFocus");
    }

    void DroidBlaster::onLostFocus() {
        packt::Log::info("onLostFocus");
    }
}
