package com.joutvhu.fixedwidth.parser.convert;

import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.ParseContext;
import com.joutvhu.fixedwidth.parser.support.Phase;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Interface thống nhất cho tất cả hook trong pipeline fixed-width.
 *
 * <p>Thay thế hoàn toàn {@code FixedWidthReader}, {@code FixedWidthWriter},
 * và {@code AnnotationHandler}. Một Hook duy nhất xử lý cả parse lẫn export
 * bằng cách kiểm tra {@code ctx.getPhase()} bên trong {@link #handle}.
 *
 * <p><b>Vòng đời:</b>
 * <ul>
 *   <li>Annotation-hook: khởi tạo mới cho mỗi lần gọi {@link #handle} (per-invocation).</li>
 *   <li>Module-hook: singleton — khởi tạo một lần khi đăng ký vào module.</li>
 * </ul>
 *
 * <p><b>Thread-safety:</b> Module-hook phải thread-safe vì instance được tái sử dụng
 * qua nhiều lần parse/export đồng thời. Annotation-hook không cần thread-safe.
 *
 * @author Giao Ho
 * @since 3.0.0
 */
public interface Hook {

    /**
     * Thực thi logic của hook tại phase hiện tại.
     */
    void handle(FixedTypeInfo info, ParseContext ctx);

    /**
     * Tập hợp các phase mà hook này muốn được gọi.
     * Default: tất cả các phase.
     */
    default Set<Phase> getSupportedPhases() {
        return EnumSet.allOf(Phase.class);
    }

    /**
     * Tên các field phải được xử lý trước field này.
     * Default: tập rỗng.
     */
    default Set<String> getDependencies(FixedTypeInfo info) {
        return Collections.emptySet();
    }
}
