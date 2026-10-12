// WideMelon renderer profile (Android port).
// Based on https://github.com/pruefsumme/widemelon
// SPDX-License-Identifier: GPL-3.0-or-later
#pragma once

#include <cstdint>

namespace WideMelon
{

// View width is fixed for an emulation session so geometry and GL allocations agree.
// Call SetWidth() before the OpenGL/Compute renderer is created/resized.
inline int& WidthStorage()
{
    static int width = 256;
    return width;
}

inline void SetWidth(int width)
{
    if (width < 256 || width > 768 || (width % 2) != 0)
        width = 256;
    WidthStorage() = width;
}

inline int Width()
{
    return WidthStorage();
}

inline bool Enabled()
{
    return Width() > 256;
}

inline int NativeWidth()
{
    return 256;
}

inline int SidePad()
{
    return (Width() - NativeWidth()) / 2;
}

// Keep original world scale and center; wider raster reveals extra columns.
// Rounded division avoids the truncating bias that shows up as dashed tile seams.
inline int32_t ProjectX(int32_t x)
{
    const int64_t w = Width();
    if (w == NativeWidth())
        return x;

    const int64_t n = NativeWidth();
    const int64_t xx = static_cast<int64_t>(x) * n;
    if (xx >= 0)
        return static_cast<int32_t>((xx + w / 2) / w);
    return static_cast<int32_t>((xx - w / 2) / w);
}

// Map native hires X (1/16 px) into wide framebuffer pixels.
inline uint32_t MapHiresX(uint32_t hiresX, int scaleFactor)
{
    const uint64_t denom = 16ull * 256ull;
    const uint64_t numer =
        static_cast<uint64_t>(hiresX) *
        static_cast<uint64_t>(scaleFactor) *
        static_cast<uint64_t>(Width());
    return static_cast<uint32_t>((numer + denom / 2) / denom);
}

// Map native final X into wide framebuffer pixels.
inline uint32_t MapFinalX(uint32_t finalX, int scaleFactor)
{
    const uint64_t denom = 256ull;
    const uint64_t numer =
        static_cast<uint64_t>(finalX) *
        static_cast<uint64_t>(scaleFactor) *
        static_cast<uint64_t>(Width());
    return static_cast<uint32_t>((numer + denom / 2) / denom);
}

}
