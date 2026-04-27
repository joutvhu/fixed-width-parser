package com.joutvhu.fixedwidth.parser.convert;

import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;

/**
 * Hook đăng ký vào {@link com.joutvhu.fixedwidth.parser.module.FixedModule},
 * tự quyết định có xử lý field này không qua {@link #supports}.
 *
 * <p>Thay thế cặp {@code FixedWidthReader} + {@code FixedWidthWriter} cũ.
 * Dispatcher chỉ gọi {@link #handle} khi {@link #supports} trả về {@code true}.
 *
 * <p><b>Lifecycle:</b> singleton — một instance duy nhất được tạo khi đăng ký
 * vào module và được tái sử dụng cho tất cả lần parse/export. Implementation
 * phải thread-safe.
 *
 * @author Giao Ho
 * @since 3.0.0
 */
public interface ModuleHook extends Hook {

    /**
     * Kiểm tra hook này có thể xử lý field/type được mô tả bởi {@code info} không.
     * Được gọi trước {@link #handle} — dispatcher không bao giờ gọi {@code handle}
     * khi method này trả về {@code false}.
     */
    boolean supports(FixedTypeInfo info);
}
