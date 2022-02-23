package org.hyt.hytport.graphics.api.model

interface HYTGLStaticAttribute : HYTGLAttribute{

    fun getStaticData(): Any;

    fun setStaticData(data: Any): Unit;

}