package org.apache.jmeter.java.ffmpeg;
 
 import org.apache.jmeter.config.Arguments;
 import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
 import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
 import org.apache.jmeter.samplers.SampleResult;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class PushwithFfmpeg extends AbstractJavaSamplerClient
 {
   private static final Logger logger = LoggerFactory.getLogger(PushwithFfmpeg.class);
 
   public void setupTest(JavaSamplerContext arg0)
   {
     logger.info("======================================start=====================================");
   }
   public void teardownTest(JavaSamplerContext arg0)
   {
     logger.info("======================================end=====================================");
   }
 
   public Arguments getDefaultParameters()
   {
     Arguments args = new Arguments();
     args.addArgument("ffmpeg", "");
     args.addArgument("pushurl", "${pushurl}");
    args.addArgument("videopath", "");
     return args;
   }
 
   public SampleResult runTest(JavaSamplerContext arg0)
   {
     logger.info("Start push stream!");
     SampleResult sr = new SampleResult();
     sr.setSampleLabel("ffmpeg_push");
 
     sr.sampleStart();
     FfmpegUtil ffmpegUtil = new FfmpegUtil();
     String[] strarray = ffmpegUtil.Push(arg0.getParameter("ffmpeg"), arg0.getParameter("videopath"), 
       arg0.getParameter("pushurl"));
     sr.sampleEnd();
 
     if (ffmpegUtil.getExitValue() == 1) {
    	 sr.setRequestHeaders(
         "ffmpeg:  " + arg0.getParameter("ffmpeg").toString() + "\n" + 
         "pushurl:  " + arg0.getParameter("pushurl").toString() + "\n" + 
         "videopath  :" + arg0.getParameter("videopath").toString());
 
       sr.setResponseData("Params Error!\n" + strarray[1].toString() + "\n**********\n" + strarray[0].toString(), 
         null);
       sr.setDataType("text");
       sr.setSuccessful(false);
     }
    else if (ffmpegUtil.getExitValue() == 0) {
       sr.setRequestHeaders(
         "ffmpeg:  " + arg0.getParameter("ffmpeg").toString() + "\n" + 
         "pushurl:  " + arg0.getParameter("pushurl").toString() + "\n" + 
         "videopath:  " + arg0.getParameter("videopath").toString());
 
       sr.setResponseData(
         "Push Successful!\n" + strarray[1].toString() + "\n**********\n" + strarray[0].toString(), null);
       sr.setDataType("text");
       sr.setSuccessful(true);
     } else if (ffmpegUtil.getExitValue() == -1) {
       sr.setRequestHeaders(
        "ffmpeg:  " + arg0.getParameter("ffmpeg").toString() + "\n" + 
         "pushurl:  " + arg0.getParameter("pushurl").toString() + "\n" + 
         "videopath:  " + arg0.getParameter("videopath").toString());
 
       sr.setResponseData("Push Fail!\n" + strarray[1].toString() + "\n**********\n" + strarray[0].toString(), 
         null);
       sr.setDataType("text");
       sr.setSuccessful(false);
     }
     return sr;
   }
 
   public static void main(String[] args) {
     FfmpegUtil ffmpegUtil = new FfmpegUtil();
     String[] str = ffmpegUtil.Push("D:/Jmeter/jmeter/ffmpeg-20160629-57d30fd-win64-static/bin", "C:/Users/admin/Desktop/video/mb.mp4", 
       "rtmp://push.live.sioeye.cn/sioeyecn/qmyABr-734c6259b2af44328ae1dcf253b058bd?liveId=734c6259b2af44328ae1dcf253b058bd");
 
     System.out.println(str[0] + "\n" + str[1]);
   }
 }

