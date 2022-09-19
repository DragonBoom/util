/**
 * 
 */
package indi.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Nullable;

import indi.data.Three;
import indi.data.Wrapper.ObjectWrapper;
import indi.exception.WrapperException;
import indi.io.IOUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 用于执行系统指令的工具
 * 
 * @author wzh
 * @since 2020.09.18
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProcessUtils {
    private static final int PROCESS_THREAD_COUNT = 2;
    /**
     * WARN: 为了处理同时有输入流与错误流的情况，使用线程池，以不阻塞地同时读取输入流与错误流
     * 
     * @param processBuilder
     * @param processDir
     * @return
     * @throws IOException
     * @since 2020.09.30
     */
    private static Three<String, String, Integer> process(ProcessBuilder processBuilder, @Nullable File processDir) throws IOException {
        if (processDir != null) {
            processBuilder.directory(processDir);// 在指定路径执行
        }
        /** 负责并发地读取流 */
        ExecutorService pool = Executors.newFixedThreadPool(PROCESS_THREAD_COUNT);// 为了能及时关闭，不共用线程池
        Process process = processBuilder.start();
        // 获取执行结果
        try(final InputStream in = process.getInputStream();
                final InputStream errorIn = process.getErrorStream()) {
            // 主动关闭输出流，避免阻塞
//            OutputStream outputStream = process.getOutputStream();
//            outputStream.close();
            final CountDownLatch latch = new CountDownLatch(2);
            final ObjectWrapper<String> inputStringWrapper = ObjectWrapper.of(null);
            Runnable readInFun = () -> {
                try {
                    String inputStr = Optional.ofNullable(in).map(IOUtils::toString).orElse(null);
                    inputStringWrapper.setValue(inputStr);
                } finally {
                    latch.countDown();
                }
            };
            final ObjectWrapper<String> errorStringWrapper = ObjectWrapper.of(null);
            Runnable readErrorFun = () -> {
                try {
                    String errorStr = Optional.ofNullable(errorIn).map(IOUtils::toString).orElse(null);
                    errorStringWrapper.setValue(errorStr);
                } finally {
                    latch.countDown();
                }
            };
//            String result = Optional.ofNullable(in).map(IOUtils::toString).orElse(null);
//            String error = Optional.ofNullable(errorIn).map(IOUtils::toString).orElse(null);
            pool.submit(readInFun);
            pool.submit(readErrorFun);
            
            latch.await();
            String result = inputStringWrapper.getValue();
            String error = errorStringWrapper.getValue();
            process.waitFor();
            return Three.of(result, error, process.exitValue());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WrapperException(e);
        } finally {
            process.destroy();// 结束命令
            pool.shutdown();
        }
    }

    /**
     * 执行指令；将主动关闭输出流，适用于不需要输入的场景；当指令无法依靠空格分隔时，应使用process(String[], File)
     * 
     * @param command 必须能够按空格分隔成独立指令
     * @param processDir 执行目录
     * @return <\result, error, process.exitValue()>，result, error均可能为null
     * @author DragonBoom
     * @since 2020.09.18
     */
    public static Three<String, String, Integer> process(String command, @Nullable File processDir) throws IOException {
        return process(new ProcessBuilder(command.split(" ")), processDir);
    }
    
    /**
     * @see #process(String, File)
     * 
     * <p>当指令无法依靠空格分隔时使用该方法
     * 
     * @param commands 
     * @param processDir
     * @return
     * @throws IOException
     * @since 2020.09.30
     */
    public static Three<String, String, Integer> process(List<String> commands, @Nullable File processDir) throws IOException {
        return process(new ProcessBuilder(commands), processDir);
    }
    
    public static Three<String, String, Integer> process(String[] commands, @Nullable File processDir) throws IOException {
        return process(new ProcessBuilder(commands), processDir);
    }
    
    public static Three<String, String, Integer> processUTF8(String[] commands, @Nullable File processDir) throws IOException {
        // FIXME:
        return null;
    }
    
    /**
     * 执行指令；将主动关闭输出流，适用于不需要输入的场景
     * 
     * @param command
     * @return <\result, error, process.exitValue()>，result, error均可能为null
     * @throws IOException
     * @author DragonBoom
     * @since 2020.09.18
     */
    public static Three<String, String, Integer> process(String command) throws IOException {
        return process(command, null);
    }
}
