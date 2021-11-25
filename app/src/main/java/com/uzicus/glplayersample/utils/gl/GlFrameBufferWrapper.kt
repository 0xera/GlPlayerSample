package com.uzicus.glplayersample.utils.gl

import android.opengl.GLES20

class GlFrameBufferWrapper {

    private var frameBufferName = 0
    private var renderBufferName = 0

    var texName = 0
        private set

    var width = -1
        private set
    var height = -1
        private set

    fun recordFrame(drawFrame: () -> Unit) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferName)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        drawFrame()
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
    }

    fun setup(width: Int, height: Int) {
        val args = IntArray(1)

        GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, args, 0)
        require(!(width > args[0] || height > args[0])) { "GL_MAX_TEXTURE_SIZE " + args[0] }

        GLES20.glGetIntegerv(GLES20.GL_MAX_RENDERBUFFER_SIZE, args, 0)
        require(!(width > args[0] || height > args[0])) { "GL_MAX_RENDERBUFFER_SIZE " + args[0] }

        GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, args, 0)
        val saveFramebuffer = args[0]

        GLES20.glGetIntegerv(GLES20.GL_RENDERBUFFER_BINDING, args, 0)
        val saveRenderbuffer = args[0]

        GLES20.glGetIntegerv(GLES20.GL_TEXTURE_BINDING_2D, args, 0)
        val saveTexName = args[0]

        release()

        this.width = width
        this.height = height

        try {
            GLES20.glGenFramebuffers(args.size, args, 0)
            frameBufferName = args[0]
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferName)
            GLES20.glGenRenderbuffers(args.size, args, 0)
            renderBufferName = args[0]
            GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, renderBufferName)
            GLES20.glRenderbufferStorage(
                GLES20.GL_RENDERBUFFER,
                GLES20.GL_DEPTH_COMPONENT16,
                width,
                height)
            GLES20.glFramebufferRenderbuffer(
                GLES20.GL_FRAMEBUFFER,
                GLES20.GL_DEPTH_ATTACHMENT,
                GLES20.GL_RENDERBUFFER,
                renderBufferName)
            texName = EglUtils.createTexture(GLES20.GL_TEXTURE_2D, GLES20.GL_LINEAR, GLES20.GL_NEAREST)
            GLES20.glTexImage2D(
                GLES20.GL_TEXTURE_2D,
                0,
                GLES20.GL_RGBA,
                width,
                height,
                0,
                GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE,
                null
            )
            GLES20.glFramebufferTexture2D(
                GLES20.GL_FRAMEBUFFER,
                GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D,
                texName,
                0
            )
            val status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER)
            if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
                throw RuntimeException("Failed to initialize framebuffer object $status")
            }
        } catch (e: RuntimeException) {
            release()
            throw e
        }

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, saveFramebuffer)
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, saveRenderbuffer)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, saveTexName)
    }

    fun release() {
        if (width == 0 && height == 0) return

        width = 0
        height = 0

        val args = IntArray(1)
        args[0] = texName
        GLES20.glDeleteTextures(args.size, args, 0)
        texName = 0
        args[0] = renderBufferName
        GLES20.glDeleteRenderbuffers(args.size, args, 0)
        renderBufferName = 0
        args[0] = frameBufferName
        GLES20.glDeleteFramebuffers(args.size, args, 0)
        frameBufferName = 0
    }

}