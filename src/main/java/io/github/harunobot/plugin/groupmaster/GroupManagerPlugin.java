/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.harunobot.plugin.groupmaster;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.harunobot.async.BotResponseCallback;
import io.github.harunobot.constant.Permissions;
import io.github.harunobot.database.SessionManager;
import io.github.harunobot.plugin.HarunoPlugin;
import io.github.harunobot.plugin.PluginFilter;
import io.github.harunobot.plugin.PluginHandler;
import io.github.harunobot.plugin.data.PluginDescription;
import io.github.harunobot.plugin.data.PluginFilterParameter;
import io.github.harunobot.plugin.data.PluginHandlerMatcher;
import io.github.harunobot.plugin.data.PluginRegistration;
import io.github.harunobot.pojo.type.Permission;
import io.github.harunobot.plugin.data.type.PluginMatcherType;
import io.github.harunobot.plugin.data.type.PluginReceivedType;
import io.github.harunobot.plugin.data.type.PluginTextType;
import io.github.harunobot.plugin.groupmaster.configuration.AutoPenaltyFileConfig;
import io.github.harunobot.plugin.groupmaster.configuration.AutoPenaltyMatcherConfig;
import io.github.harunobot.plugin.groupmaster.configuration.Configuration;
import io.github.harunobot.plugin.groupmaster.configuration.ManualPenaltyConfig;
import io.github.harunobot.plugin.groupmaster.data.AutoPenaltyMatcherWrapper;
import io.github.harunobot.plugin.groupmaster.data.AutoPenaltyWrapper;
import io.github.harunobot.plugin.groupmaster.data.GroupAutoPenaltyWrapper;
import io.github.harunobot.plugin.groupmaster.data.ManualPenaltyWrapper;
import io.github.harunobot.plugin.groupmaster.data.TurnoverWrapper;
import io.github.harunobot.plugin.groupmaster.db.entity.AttachmentBlob;
import io.github.harunobot.plugin.groupmaster.db.entity.UserBanRecord;
import io.github.harunobot.plugin.groupmaster.db.entity.UserBlockRecord;
import io.github.harunobot.plugin.groupmaster.db.entity.UserWarnRecord;
import io.github.harunobot.plugin.groupmaster.db.entity.GroupBanRecord;
import io.github.harunobot.plugin.groupmaster.db.type.CauseType;
import io.github.harunobot.proto.event.BotEvent;
import io.github.harunobot.proto.event.BotMessage;
import io.github.harunobot.proto.event.BotMessageRecord;
import io.github.harunobot.proto.event.type.MessageContentType;
import io.github.harunobot.proto.request.BotRequest;
import io.github.harunobot.proto.request.type.RequestType;
import io.github.harunobot.proto.response.BotResponse;
import io.github.harunobot.proto.response.type.StatusType;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.ext.web.client.HttpResponse;
//import io.reactivex.Observable;
//import io.reactivex.disposables.Disposable;
//import io.vertx.reactivex.ext.web.client.HttpRequest;
//import io.vertx.reactivex.ext.web.client.HttpResponse;
//import io.vertx.reactivex.ext.web.client.predicate.ResponsePredicate;
//import io.vertx.reactivex.core.CompositeFuture;
//import io.vertx.reactivex.core.buffer.Buffer;
//import io.vertx.reactivex.core.file.AsyncFile;
//import io.vertx.reactivex.ext.web.codec.BodyCodec;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Environment;
import org.hibernate.query.Query;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.zeroturnaround.zip.ZipUtil;

/**
 *
 * @author iTeam_VEP
 */
public class GroupManagerPlugin extends HarunoPlugin implements PluginFilter, PluginHandler {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(GroupManagerPlugin.class);
    
    private static final String PLUGIN_NAME = "Haruno Group Master";
    private static final int LIMIT_CACHE_SIZE = 100;
    private static final int DEFAULT_BLOCK_DURATION = 30*60;
    private final List<GroupAutoPenaltyWrapper> globalMatcheres = new ArrayList();
    private final Map<String, AutoPenaltyWrapper> autoPenalties = new HashMap();
    private final Map<Long, List<GroupAutoPenaltyWrapper>> groupMatcheres = new HashMap();
    private final Map<Long, TurnoverWrapper> groupTurnover = new HashMap();
    private final Set<Long> superAdmin = new HashSet();
    private final ObjectOpenHashSet<String> blockedUsers = new ObjectOpenHashSet();
    private final Map<String, Long> blockedUserTimers = new HashMap();
    
    private ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    private Cache<String, List<Long>> silenceKickTask = Caffeine.newBuilder()
                                .expireAfterAccess(24, TimeUnit.HOURS)
                                .maximumSize(LIMIT_CACHE_SIZE)
                                .build();
    private String tempPath;
    private ManualPenaltyWrapper manualPenaltyWrapper = null;
    private SessionManager sessionManager;
    private String path;
    private boolean enable = false;
    

    public GroupManagerPlugin(){
        super(new PluginDescription.Builder().id("io.github.harunobot.plugin.groupmaster").name("group master").version("0.1.0").build());
    }

    @Override
    public PluginRegistration onLoad(String path) throws Exception {
        MDC.put("module", PLUGIN_NAME);
        this.path = path;
        this.tempPath = System.getProperty("java.io.tmpdir");
        if(!this.tempPath.endsWith("/") && !this.tempPath.endsWith("\\")){
            this.tempPath = this.tempPath+"/";
        }
        this.tempPath = this.tempPath+"skymas/";
        configure();
        
        Set<Permission> permissions = new HashSet();
        permissions.addAll(Permissions.PUBLIC_MESSAGE);
        permissions.addAll(Permissions.HANDLE_SOCIAL_REQUEST);
        permissions.addAll(Permissions.PUBLIC_MEMBER_CHANGED_NOTICE);
        
        Map<PluginFilterParameter, PluginFilter> filters = new HashMap();
        PluginFilterParameter parameter = new PluginFilterParameter(
                PluginReceivedType.PUBLIC,
                "group management",
                1
        );
        filters.put(parameter, this);
        if(manualPenaltyWrapper == null){
            return new PluginRegistration(permissions, null, filters, this);
        }
        Map<PluginHandlerMatcher, PluginHandler> handlers = new HashMap();
        if(manualPenaltyWrapper.mute() != null){
            PluginHandlerMatcher matcher = new PluginHandlerMatcher(
                PluginReceivedType.PUBLIC,
                manualPenaltyWrapper.mute(),
                manualPenaltyWrapper.splitChar()
            );
            handlers.put(matcher, (String params, BotEvent event) -> {
                if(!enable){
                    return null;
                }
                if(!manualPenaltyWrapper.allow(event.groupId(), event.userId())){
                    handleAbuse(event);
                    return null;
                }
                if(event.messages()[0].messageType() == MessageContentType.REPLY){
                    BotResponseCallback<BotEvent> cb = (BotResponse<BotEvent> response)->{
                        if(response.getStatus() == StatusType.FAILED){
                            return;
                        }
                        handleManualMutePenalty(params, event, response.getData());
                    };
                    fetchMessage(getReplySourceMessageId(event), cb);
                } else {
                    handleManualMutePenalty(params, event, null);
                }
                return null;
            });
        }
        if(manualPenaltyWrapper.block()!= null){
            PluginHandlerMatcher matcher = new PluginHandlerMatcher(
                PluginReceivedType.PUBLIC,
                manualPenaltyWrapper.block(),
                manualPenaltyWrapper.splitChar()
            );
            handlers.put(matcher, (String params, BotEvent event) -> {
                if(!enable){
                    return null;
                }
                if(!manualPenaltyWrapper.allow(event.groupId(), event.userId())){
                    handleAbuse(event);
                    return null;
                }
                if(event.messages()[0].messageType() == MessageContentType.REPLY){
                    BotResponseCallback<BotEvent> cb = (BotResponse<BotEvent> response)->{
                        if(response.getStatus() == StatusType.FAILED){
                            return;
                        }
                        handleManualBlockPenalty(params, event, response.getData());
                    };
                    fetchMessage(getReplySourceMessageId(event), cb);
                } else {
                    handleManualBlockPenalty(params, event, null);
                }
                return null;
            });
        }
        if(manualPenaltyWrapper.ban()!= null){
            PluginHandlerMatcher matcher = new PluginHandlerMatcher(
                PluginReceivedType.PUBLIC,
                manualPenaltyWrapper.ban(),
                manualPenaltyWrapper.splitChar()
            );
            handlers.put(matcher, (String params, BotEvent event) -> {
                if(!enable){
                    return null;
                }
//                if(params.equals(manualPenaltyWrapper.globalBan())){
//                    return null;
//                }
                if(!manualPenaltyWrapper.allow(event.groupId(), event.userId())){
                    handleAbuse(event);
                    return null;
                }
                AtomicBoolean rejectJoin = new AtomicBoolean(false);
                if(groupTurnover.containsKey(event.groupId()) && groupTurnover.get(event.groupId()).isRejectJoin()){
                    rejectJoin.set(true);
                }
                if(event.messages()[0].messageType() == MessageContentType.REPLY){
                    BotResponseCallback<BotEvent> cb = (BotResponse<BotEvent> response)->{
                        if(response.getStatus() == StatusType.FAILED){
                            return;
                        }
                        handleManualBanPenalty(params, event, response.getData(), rejectJoin.get(), false);
                    };
                    fetchMessage(getReplySourceMessageId(event), cb);
                } else {
                    handleManualBanPenalty(params, event, null, rejectJoin.get(), false);
                }
                return null;
            });
            PluginHandlerMatcher globaMmatcher = new PluginHandlerMatcher(
                PluginReceivedType.PUBLIC,
                manualPenaltyWrapper.globalBan(),
                manualPenaltyWrapper.splitChar()
            );
            handlers.put(globaMmatcher, (String params, BotEvent event) -> {
                if(!enable){
                    return null;
                }
                if(!manualPenaltyWrapper.allowGlobal(event.userId())){
                    handleAbuse(event);
                    return null;
                }
                AtomicBoolean rejectJoin = new AtomicBoolean(false);
                if(groupTurnover.containsKey(event.groupId()) && groupTurnover.get(event.groupId()).isRejectJoin()){
                    rejectJoin.set(true);
                }
                if(event.messages()[0].messageType() == MessageContentType.REPLY){
                    BotResponseCallback<BotEvent> cb = (BotResponse<BotEvent> response)->{
                        if(response.getStatus() == StatusType.FAILED){
                            return;
                        }
                        handleManualBanPenalty(params, event, response.getData(), rejectJoin.get(), true);
                    };
                    fetchMessage(getReplySourceMessageId(event), cb);
                } else {
                    handleManualBanPenalty(params, event, null, rejectJoin.get(), true);
                }
                return null;
            });
        }
        MDC.clear();
        return new PluginRegistration(permissions, handlers, filters, this);
    }
    
    @Override
    public boolean toNext(BotEvent event) {
        if(!enable){
            return true;
        }
        MDC.put("module", PLUGIN_NAME);
        String uuid = generateUuid(event.groupId(), event.userId());
        TurnoverWrapper turnoverWrapper = groupTurnover.get(event.groupId());
        if(turnoverWrapper != null && turnoverWrapper.existSilenceUser()){
            List<Long> tasks = silenceKickTask.getIfPresent(uuid);
            if(tasks != null){
                vertx().cancelTimer(tasks.get(0));
                vertx().cancelTimer(tasks.get(1));
                tasks.clear();
                turnoverWrapper.removeSilenceUser();
                silenceKickTask.invalidate(uuid);
            }
        }
        if(!globalMatcheres.isEmpty()){
            Optional<GroupAutoPenaltyWrapper> wrapper = globalMatcheres
//                    .parallelStream()
                    .stream()
                    .filter(matcher -> !matcher.allow(event.groupId(), event.userId(), event.messages()))
                    .findFirst();
            if(wrapper.isPresent()){
                handleAutoPenalty(event, wrapper.get());
                return false;
            }
        }
        List<GroupAutoPenaltyWrapper> groupWrapper = groupMatcheres.get(event.groupId());
        if(groupWrapper != null){
            Optional<GroupAutoPenaltyWrapper> wrapper = groupWrapper
//                    .parallelStream()
                    .stream()
                    .filter(matcher -> !matcher.allow(event.groupId(), event.userId(), event.messages()))
                    .findFirst();
            if(wrapper.isPresent()){
                handleAutoPenalty(event, wrapper.get());
                return false;
            }
        }
        return !blockedUsers.contains(uuid);
    }
    
    
    @Override
    public BotRequest handle(String parameter, BotEvent event) {
        MDC.put("module", PLUGIN_NAME);
        switch(event.directiveType()){
//            https://github.com/howmanybots/onebot/blob/master/v11/specs/event/request.md
            case PUBLIC_ADD_REQUEST:{   //加群请求
                long groupId = event.groupId(); //群号
                long userId = event.userId();  //发送请求的 QQ 号
                String comment = event.comment();   //验证信息
                String flag = event.flag(); //请求 flag，在调用处理请求的 API 时需要传入
                LOG.info("member {} request to join {} {} {}", userId, groupId, comment, flag);
                if(isBanMember(groupId, userId)){
                    handleSocialAddRequest(event, false, "you have blocked by skymas");
                    return null;
                }
//                handleSocialAddRequest(event, true, null);
//                kickMember(groupId, userId, false);
                break;
            }
            case PUBLIC_INVITE_REQUEST:{    //邀请登录号入群
                long groupId = event.groupId(); //群号
                long userId = event.userId();  //发送请求的 QQ 号
                String comment = event.comment();   //验证信息
                String flag = event.flag(); //请求 flag，在调用处理请求的 API 时需要传入
                LOG.info("member {} invited you to {} {} {}", userId, groupId, comment, flag);
                if(superAdmin.contains(userId)){
                    handleSocialAddRequest(event, true, null);
                }
                break;
            }
//            https://github.com/howmanybots/onebot/blob/master/v11/specs/event/notice.md#%E7%BE%A4%E6%88%90%E5%91%98%E5%A2%9E%E5%8A%A0
            case PUBLIC_INVITE_NOTICE:{ //管理员邀请入群
                long groupId = event.groupId(); //群号
                long userId = event.userId();  //加入者 QQ 号
                long operatorId = event.operatorId();   //操作者 QQ 号(cq保留字段)
                LOG.info("member {} invited in  {} by {}", userId, groupId, operatorId);
                if(isBanMember(groupId, userId)){
                    sendGroupMessage(groupId, "warning: user " + userId +" have been blocked by skymas", -1);
                }
                if(groupTurnover.containsKey(groupId)){
                    TurnoverWrapper wrapper = groupTurnover.get(groupId);
                    if(wrapper.isEnableWelcome()){
                        sendGroupMessage(groupId,  wrapper.generateWelcome(userId), -1);
                    }
                    if(wrapper.getSilenceTimeout() > 0){
                        addSilenceTimeoutTask(groupId, userId, wrapper);
                    }
                }
                break;
            } 
            case PUBLIC_APPROVED_NOTICE:{   //管理员已同意入群
                long groupId = event.groupId(); //群号
                long userId = event.userId();  //离开者 QQ 号
                long operatorId = event.operatorId();   //操作者 QQ 号(cq保留字段)
                LOG.info("member {} joined {} by {}", userId, groupId, operatorId);
                if(isBanMember(groupId, userId)){
                    kickMember(groupId, userId, false);
                    return fastReply(RequestType.MESSAGE_PUBLIC, "user " + userId +" have been kicked by skymas");
                }
                if(groupTurnover.containsKey(groupId)){
                    TurnoverWrapper wrapper = groupTurnover.get(groupId);
                    if(wrapper.isEnableWelcome()){
                        sendGroupMessage(groupId, wrapper.generateWelcome(userId), -1);
                    }
                    if(wrapper.getSilenceTimeout() > 0){
                        addSilenceTimeoutTask(groupId, userId, wrapper);
                    }
                }
                break;
            }
            case PUBLIC_LEAVE_NOTICE:{  //leave
                long groupId = event.groupId(); //群号
                long userId = event.userId();  //离开者 QQ 号
                long operatorId = event.operatorId();   //操作者 QQ 号（如果是主动退群，则和 user_id 相同）
                if(userId == operatorId){
                    LOG.info("LEAVE: member {} leave {}", userId, groupId);
                } else {
                    LOG.info("LEAVE: member {} was kick by {} from {}", userId, operatorId, groupId);
                }
                if(groupTurnover.containsKey(groupId)){
                    TurnoverWrapper wrapper = groupTurnover.get(groupId);
                    if(wrapper.isEnableFarewell()){
                        sendGroupMessage(groupId, wrapper.generateFarewell(userId), -1);
                    }
                    if(wrapper.isBanLeave()){
                        banMember(null, groupId, userId, 0, CauseType.LEAVE, null, false);
                    }
                }
                break;
            }
            case KICKED_NOTICE:{    //成员被踢
                long groupId = event.groupId(); //群号
                long userId = event.userId();  //离开者 QQ 号
                long operatorId = event.operatorId();   //操作者 QQ 号（如果是主动退群，则和 user_id 相同）
                if(userId == operatorId){
                    LOG.info("KICKED: member {} leave {}", userId, groupId);
                } else {
                    LOG.info("KICKED: member {} was kick by {} from {}", userId, operatorId, groupId);
                }
                if(groupTurnover.containsKey(groupId)){
                    TurnoverWrapper wrapper = groupTurnover.get(groupId);
                    if(wrapper.isEnableFarewell()){
                        return fastReply(RequestType.MESSAGE_PUBLIC
                            , wrapper.generateFarewell(userId));
                    }
                }
                break;
            }
            case KICKED_ME_NOTICE:{ //登录号被踢
                long groupId = event.groupId(); //群号
                long userId = event.userId();  //离开者 QQ 号
                long operatorId = event.operatorId();   //操作者 QQ 号（如果是主动退群，则和 user_id 相同）
                if(userId == operatorId){
                    LOG.info("KICKED_ME: bot {} leave {}", userId, groupId);
                } else {
                    LOG.warn("KICKED_ME: bot {} was kick by {} from {}", userId, operatorId, groupId);
                }
                break;
            }
            default:{
                break;
            }
        }
        MDC.clear();
        return null;
    }
    
    private void addSilenceTimeoutTask(long groupId, long userId, TurnoverWrapper wrapper){
//        Map<Long, List<Long>> task = silenceKickTask.getIfPresent(generateUuid(groupId, userId));
//        if(task == null){
//            task = new HashMap();
//        }
        List<Long> timers = new ArrayList();
        long reminder = vertx().setTimer(TimeUnit.SECONDS.toMillis(wrapper.getSilenceTimeout())/2, time -> {
            sendGroupMessageWithMention(groupId, userId, wrapper.getSilenceTimeoutWarning(), -1);
        });
        timers.add(reminder);
        long kicker = vertx().setTimer(TimeUnit.SECONDS.toMillis(wrapper.getSilenceTimeout()), time -> {
            kickMember(groupId, userId, wrapper.isRejectJoin());
            if(wrapper.isBanIfSilenceTimeout()){
                banMember(null, groupId, userId, 0, CauseType.TIMEOUT, null, false);
            }
        });
        timers.add(kicker);
        silenceKickTask.put(generateUuid(groupId, userId), timers);
        wrapper.addSilenceUser();
    }
    
//    https://www.tutorialspoint.com/hibernate/hibernate_query_language.htm
    
    final String isBanMemberHql = "FROM UserBanRecord where userId=:userId";  
    
    private boolean isBanMember(long groupId, long userId){
        if(sessionManager == null){
            LOG.warn("database not configurated");
        }
        try(Session session = sessionManager.openSession();){
            Query<UserBanRecord> isBanMemberQuery = session.createQuery(isBanMemberHql);
            isBanMemberQuery.setParameter("userId", userId);
            List<UserBanRecord> isBanMemberList = isBanMemberQuery.getResultList();
            
            if(isBanMemberList.isEmpty()){
                return false;
            }
            for(UserBanRecord userBanRecord:isBanMemberList){
                if(!userBanRecord.isActivated()){
                    continue;
                }
                if(groupId == userBanRecord.getGroupId()){
                    return true;
                }
                if(userBanRecord.isGlobal()){
                    return true;
                }
            }
            return false;
        }
    }
    
    
    final String warnMemberHql = "FROM UserWarnRecord where userId=:userId and groupId=:groupId and penaltyId=:penaltyId";  
    
    private void warnMember(BotMessage[] messages, long groupId, long userId, GroupAutoPenaltyWrapper wrapper){
        if(sessionManager == null){
            LOG.warn("database not configurated");
        }
        String penaltyId = wrapper.id();
        try(Session session = sessionManager.openSession();){
            Query<UserWarnRecord> query = session.createQuery(warnMemberHql);
            query.setParameter("userId", userId);
            query.setParameter("groupId", groupId);
            query.setParameter("penaltyId", penaltyId);
            int count = query.getResultList().size();
            
            Transaction tx = session.beginTransaction();
            if(wrapper.counted()){
                if(wrapper.banMaxCount() > 0 && (count + 1 >= wrapper.banMaxCount())){
                    UserBanRecord banRecord = new UserBanRecord();
                    banRecord.setActivated(true);
                    banRecord.setGlobal(false);
                    banRecord.setCause(penaltyId);
                    banRecord.setCauseType(CauseType.REACHLIMIT);
                    banRecord.setOperatorId(0);
                    banRecord.setGroupId(groupId);
                    banRecord.setUserId(userId);
                    banRecord.setMessages(BotMessageRecord.convertBotMessages(messages));
                    banRecord.setCreatetime(Instant.now());
                    
                    String folderName = new StringBuilder().append(groupId).append("-").append(userId).append("-").append("warn").append(Instant.now()).toString();
                    saveAttachmentBolbEntity(folderName, messages, banRecord);
                    
                    kickMember(groupId, userId, false);
                    LOG.info("member {} in {} has been kick by reach limit {}", userId, groupId, penaltyId); 
                    int warningCount = removeWarnings(groupId, userId, penaltyId, session);
                    LOG.info("remove {} {} {} warningCount {}", groupId, userId, penaltyId, warningCount); 
                    return;
                }
                if(wrapper.muteMaxCount() > 0 && (count + 1 >= wrapper.muteMaxCount())){
                    muteUser(groupId, userId, wrapper.maxMuteDuration());
                    if(wrapper.muteMaxCount() > wrapper.banMaxCount()){
                        int warningCount = removeWarnings(groupId, userId, penaltyId, session);
                        LOG.info("remove {} {} {} warningCount {}", groupId, userId, penaltyId, warningCount);
                        return;
                    }
                }
                
                UserWarnRecord warnRecord = new UserWarnRecord();
                warnRecord.setPenaltyId(penaltyId);
                warnRecord.setGroupId(groupId);
                warnRecord.setUserId(userId);
                warnRecord.setMessages(BotMessageRecord.convertBotMessages(messages));
                warnRecord.setCreatetime(Instant.now());

                String folderName = new StringBuilder().append(groupId).append("-").append(userId).append("-").append("warn").append(Instant.now()).toString();
                saveAttachmentBolbEntity(folderName, messages, warnRecord);
            }
            tx.commit();
        }
    }
    
    final String findBlockMemberHql = "FROM UserBlockRecord where userId=:userId and groupId=:groupId";  
    
    private UserBlockRecord findBlockMember(long groupId, long userId){
        if(sessionManager == null){
            LOG.warn("database not configurated");
        }
        UserBlockRecord record = null;
        try(Session session = sessionManager.openSession();){
            Query<UserBlockRecord> query = session.createQuery(findBlockMemberHql);
            query.setParameter("userId", userId);
            query.setParameter("groupId", groupId);
            List<UserBlockRecord> blockMembers = query.getResultList();
            
            if(blockMembers.isEmpty()){
                return null;
            }
            Transaction tx = session.beginTransaction();
            long timestamp = Instant.now().toEpochMilli();
            long maxEndTime = 0;
            for(UserBlockRecord userBlockRecord:blockMembers){
                if(!userBlockRecord.isActivated()){
                    continue;
                }
                long endTime = userBlockRecord.getCreatetime().toEpochMilli()+(userBlockRecord.getDuration()*1000);
                if(endTime < timestamp){
                    session.delete(userBlockRecord);
                    continue;
                }
                if(endTime > maxEndTime){
                    maxEndTime = endTime;
                    record =  userBlockRecord;
                }
            }
            tx.commit();
        }
        return record;
    }
    
    private void blockMember(BotMessage[] messages, long groupId, long userId, long operatorId, int duration){
        if(sessionManager == null){
            LOG.warn("database not configurated");
        }
        UserBlockRecord blockRecord = new UserBlockRecord();
        blockRecord.setActivated(true);
        blockRecord.setCause(null);
        blockRecord.setCauseType(CauseType.MANUAL);
        blockRecord.setOperatorId(operatorId);
        blockRecord.setGroupId(groupId);
        blockRecord.setUserId(userId);
        blockRecord.setDuration(duration);
        blockRecord.setMessages(BotMessageRecord.convertBotMessages(messages));
        blockRecord.setCreatetime(Instant.now());
        //blockRecord.setAttachment(fetchRecordBinaryData(messages));

        String folderName = new StringBuilder().append(groupId).append("-").append(userId).append("-").append("block").append(Instant.now()).toString();
        saveAttachmentBolbEntity(folderName, messages, blockRecord);
    }
    
    private void banMember(BotMessage[] messages, long groupId, long userId, long operatorId, CauseType causeType, String cause, boolean global){
        if(sessionManager == null){
            LOG.warn("database not configurated");
            return;
        }
        UserBanRecord banRecord = new UserBanRecord();
        banRecord.setActivated(true);
        banRecord.setGlobal(global);
        banRecord.setCause(cause);
        banRecord.setCauseType(causeType);
        banRecord.setOperatorId(operatorId);
        banRecord.setGroupId(groupId);
        banRecord.setUserId(userId);
        banRecord.setMessages(BotMessageRecord.convertBotMessages(messages));
        banRecord.setCreatetime(Instant.now());
        //banRecord.setAttachment(fetchRecordBinaryData(messages));

        String folderName = new StringBuilder().append(groupId).append("-").append(userId).append("-").append("ban").append(Instant.now()).toString();
        saveAttachmentBolbEntity(folderName, messages, banRecord);
    }
    
    private void saveAttachmentBolbEntity(String folderName, BotMessage[] messages, AttachmentBlob attachmentBlob){
        if(messages == null){
            try(Session session = sessionManager.openSession();){
                Transaction tx = session.beginTransaction();
                session.save(attachmentBlob);
                tx.commit();
            }
            return;
        }
        
        List<Future> futures = new ArrayList();
        String folder = new StringBuilder().append(tempPath).append(folderName).toString();
        fetchRecordBinaryData(messages, folder, futures);

        io.vertx.core.CompositeFuture.join(futures).onComplete(ar -> {
            File zipFile = null;
            try(Session session = sessionManager.openSession();){
                Transaction tx = session.beginTransaction();
                if(ar.succeeded()){
                    File folderFile = new File(folder);
                    if(folderFile.exists()){
                        zipFile = new File(new StringBuilder().append(folder).append(".zip").toString());
                        ZipUtil.pack(folderFile, zipFile);
                        try {
                            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(zipFile));
                            attachmentBlob.setAttachment(session.getLobHelper().createBlob(bis, zipFile.length()));
                        } catch (FileNotFoundException ex) {
                            LOG.error("zip message files failed", ex);
                        }
                    }
                } else {
                    LOG.error("download message files failed", ar.cause());
                }
                session.save(attachmentBlob);
                tx.commit();
            }
            if(zipFile != null && zipFile.exists()){
                zipFile.delete();
            }
        });
    }
    
    final String removeWarningsHql = "DELETE FROM UserWarnRecord where userId=:userId and groupId=:groupId and penaltyId=:penaltyId";   
    
    private int removeWarnings(long groupId, long userId, String penaltyId, Session session){
        Query<UserWarnRecord> query = session.createQuery(removeWarningsHql);
        query.setParameter("userId", userId);
        query.setParameter("groupId", groupId);
        query.setParameter("penaltyId", penaltyId);
        return query.executeUpdate();
    }
    
    private int countWarnings(long groupId, long userId, String penaltyId){
        if(sessionManager == null){
            LOG.warn("database not configurated");
            return -1;
        }
        String hql = "FROM UserWarnRecord where userId=:userId and groupId=:groupId and penaltyId=:penaltyId";     
        System.out.println(hql);
        try(Session session = sessionManager.openSession();){
            Query<UserWarnRecord> query = session.createQuery(hql);
            query.setParameter("userId", userId);
            query.setParameter("groupId", groupId);
            query.setParameter("penaltyId", penaltyId);
            return query.getResultList().size();
        }
    }
    
    private void fetchRecordBinaryData(BotMessage[] messages, String folder, List<Future> toComplete){
        if(messages == null){
            return;
        }
        for(BotMessage botMessage:messages){
            switch(botMessage.messageType()){
            case IMAGE:{
                String filePath = new StringBuilder().append(folder).append("/").append(botMessage.file()).append("-").append(Instant.now()).append(".image").toString();
                toComplete.add(generateFetchRecordBinaryDataFuture(botMessage.url(), filePath));
                break;
            }
            case AUDIO:{
                String filePath = new StringBuilder().append(folder).append("/").append(botMessage.file()).append("-").append(Instant.now()).append(".audio").toString();
                toComplete.add(generateFetchRecordBinaryDataFuture(botMessage.url(), filePath));
                break;
            }
            case VIDEO:{
                String filePath = new StringBuilder().append(folder).append("/").append(botMessage.file()).append("-").append(Instant.now()).append(".video").toString();
                toComplete.add(generateFetchRecordBinaryDataFuture(botMessage.url(), filePath));
                break;
            }
            default:{
                continue;
            } 
            }
        }
    }
    
    private Future<HttpResponse<Buffer>> generateFetchRecordBinaryDataFuture(String url, String filePath){
        Future<HttpResponse<Buffer>> future = Future.factory.future();
        future.onSuccess(response -> {
            String contentType = response.getHeader("content-type");
            File illustrationFile;
            if(contentType != null && !contentType.isBlank()){
                illustrationFile = new File(filePath.concat("-").concat(contentType.replaceAll("\\\\", "5C").replaceAll("/", "2F")));
            } else {
                illustrationFile = new File(filePath);
            }
            AsyncFile file = vertx().fileSystem().openBlocking(illustrationFile.toPath().toString(), new OpenOptions().setCreate(true).setWrite(true));
            file.end(response.body());
            file.close();
        });
        future.onFailure(th -> {
            LOG.error("Something went wrong ", th.getCause());
        });
        webClient().getAbs(url).send(future);
        return future;
    }
    
    private String generateUuid(long groupId, long userId){
        return new StringBuilder().append(groupId).append("-").append(userId).toString();
    }
    
//    @Override
//    public void handleResponse(long sid, RequestType requestType, BotResponse response){
//        CallbackWrapper callbackWrapper = callback.getIfPresent(sid);
//        if(callbackWrapper == null){
//            return;
//        }
//        
//        if(requestType == RequestType.GET_MESSAGE){
//            BotEvent event = (BotEvent) response.getData();
//            if(event == null) {
//                LOG.warn("handleResponse bot event is null?!");
//                return;
//            }
//            switch(callbackWrapper.getType()){
//                case MUTE:{
//                    handleManualMutePenalty(callbackWrapper.getMessage(), event);
//                    break;
//                }
//                case WARN:{
//                    handleManualWarnPenalty(callbackWrapper.getMessage(), event, callbackWrapper.isGlobal());
//                    break;
//                }
//                case BAN:{
//                    handleManualBanPenalty(callbackWrapper.getMessage(), event, callbackWrapper.isGlobal());
//                    break;
//                }
//            }
//        }
//    }
    
    private void handleAutoPenalty(BotEvent event, GroupAutoPenaltyWrapper wrapper){
        MDC.put("module", PLUGIN_NAME);
        LOG.info("user {} - {} {} trigger {}", event.groupId(), event.userId(), wrapper.punishment());
        switch(wrapper.punishment()){
            case MUTE:{
                muteUser(event, wrapper.muteDuration());
                break;
            }
            case WARN:{
                BotMessage[] meaasges = new BotMessage[2];
                meaasges[0] = new BotMessage.Builder().messageType(MessageContentType.REPLY).data(String.valueOf(event.messageId())).build();
                meaasges[1] = new BotMessage.Builder().messageType(MessageContentType.TEXT).data(wrapper.warnMessage()).build();
                sendGroupMessage(event.groupId(), meaasges, -1);
                if(wrapper.counted()){
                    warnMember(event.messages(), event.groupId(), event.userId(), wrapper);
                }
                break;
            }
            case BAN:{
                BotMessage[] meaasges = new BotMessage[1];
                meaasges[0] = new BotMessage.Builder().messageType(MessageContentType.TEXT).data("auto kicked").build();
                sendGroupMessage(event.groupId(), meaasges, -1);
                banMember(event.messages(), event.groupId(), event.userId(), 0, CauseType.KEYWORD, wrapper.id(), false);
                break;
            }
            case DROP:{
                break;
            }
            case RECORD:{
                //TODO
                break;
            }
            case DEFAULT:{
                break;
            }
        }
    }
    
    private void handleAbuse(BotEvent event){
        MDC.put("module", PLUGIN_NAME);
        LOG.info("Haruno Group Manager Abuse: {} {} {}", event.groupId(), event.userId(), event.rawMessage());
        muteUser(event, 10*60);
        MDC.clear();
    }
    
    private void handleManualMutePenalty(String command, BotEvent operatorEvent, BotEvent originEvent){
//        if( message.contains(" ")){
//            String[] command = message.split(" ");
//            muteUser(event, Integer.valueOf(command[1].trim())*60);
//        } else {
//            muteUser(event, manualPenaltyWrapper.muteDuration());
//        }
        MDC.put("module", PLUGIN_NAME);
        long groupId = operatorEvent.groupId();
        long operatorId = operatorEvent.userId();
        long userId = 0;
        boolean userIdExist = false;
        if(originEvent != null){
            userId = originEvent.userId();
            userIdExist = true;
        } else {
            for(BotMessage botMessage:operatorEvent.messages()){
                if(botMessage.messageType() == MessageContentType.MENTION){
                    userId = Long.valueOf(botMessage.data());
                    userIdExist = true; 
                }
            }
        }
        if(!userIdExist){
            LOG.warn("userId not specified");
            sendGroupMessageWithMention(groupId, operatorId, "user not specified", -1);
            return;
        }
        int duration = -1;
//        BotMessage textMessage = findTextMessage(params, operatorEvent.messages());
//        if(textMessage != null){
//            String command = textMessage.data().replace(params, "").trim();
//            if(!command.isBlank()){
//                try{
//                    duration = Integer.valueOf(command);
//                }catch(NumberFormatException ex){
//                    LOG.warn("block duration number format failed {}", command);
//                }
//            }
//        }
        if(command != null){
            try{
                duration = Integer.valueOf(command);
            }catch(NumberFormatException ex){
                LOG.warn("block duration number format failed {}", command);
            }
        }
        if(duration > 0){
            muteUser(groupId, userId, duration*60);
        } else {
            muteUser(groupId, userId, manualPenaltyWrapper.muteDuration());
        }
    }
    
    private void handleManualBlockPenalty(String command, BotEvent operatorEvent, BotEvent originEvent){
//        BotMessage[] meaasges = new BotMessage[2];
//        meaasges[0] = new BotMessage.Builder().messageType(MessageContentType.REPLY).data(String.valueOf(originEvent.messageId())).build();
//        meaasges[1] = new BotMessage.Builder().messageType(MessageContentType.TEXT).data("warning +1").build();
//        sendGroupMessage(event.groupId(), meaasges, -1);
        MDC.put("module", PLUGIN_NAME);
        long groupId = operatorEvent.groupId();
        long operatorId = operatorEvent.userId();
        long userId = 0;
        boolean userIdExist = false;
        if(originEvent != null){
            userId = originEvent.userId();
            userIdExist = true;
        } else {
            for(BotMessage botMessage:operatorEvent.messages()){
                if(botMessage.messageType() == MessageContentType.MENTION){
                    userId = Long.valueOf(botMessage.data());
                    userIdExist = true; 
                }
            }
        }
        if(!userIdExist){
            LOG.warn("userId not specified");
            sendGroupMessageWithMention(groupId, operatorId, "user not specified", -1);
            return;
        }
        int duration = -1;
//        BotMessage textMessage = findTextMessage(trait, operatorEvent.messages());
//        if(textMessage != null){
//            String command = textMessage.data().replace(trait, "").trim();
//            if(!command.isBlank()){
//                try{
//                    duration = Integer.valueOf(command);
//                }catch(NumberFormatException ex){
//                    LOG.warn("block duration number format failed {}", command);
//                }
//            }
//        }
        if(command != null){
            try{
                duration = Integer.valueOf(command);
            }catch(NumberFormatException ex){
                LOG.warn("block duration number format failed {}", command);
            }
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("you have been blocked ");
        if(duration > 0){
            sb.append("for ").append(duration).append(" seconds");
        } else if(duration == 0) {
            sb.append("permanently");
        } else {
            sb.append("for ").append(DEFAULT_BLOCK_DURATION/60).append(" mins by default");
            duration = DEFAULT_BLOCK_DURATION;
        }
        UserBlockRecord userBlockRecord = findBlockMember(groupId, userId);
        if(userBlockRecord != null){
            if((userBlockRecord.getCreatetime().toEpochMilli()
                    + userBlockRecord.getDuration()*1000)
                    >= (Instant.now().toEpochMilli()+duration*1000)){
                return;
            }
        }
        String uuid = generateUuid(groupId, userId);
        long timerId;
        if(blockedUsers.contains(uuid)){
            timerId = blockedUserTimers.remove(uuid);
            vertx().cancelTimer(timerId);
        }
        timerId = vertx().setTimer(TimeUnit.SECONDS.toMillis(duration), time -> {
            blockedUsers.remove(uuid);
            blockedUserTimers.remove(uuid);
        });
        blockedUsers.add(uuid);
        blockedUserTimers.put(uuid, timerId);
        sendGroupMessageWithMention(groupId, userId, sb.toString(), -1);
        if(originEvent != null){
            blockMember(originEvent.messages(), groupId, userId, operatorId, duration);
        } else {
            blockMember(null, groupId, userId, operatorId, duration);
        }
    }
    
    private void handleManualBanPenalty(String cause, BotEvent operatorEvent, BotEvent originEvent, boolean rejectJoin, boolean global){
//        sendGroupMessage(operatorEvent.groupId(), "kicked", -1);
        MDC.put("module", PLUGIN_NAME);
        long groupId = operatorEvent.groupId();
        long operatorId = operatorEvent.userId();
        long userId = 0;
//        String cause = null;
        boolean userIdExist = false;
        if(originEvent != null){
            userId = originEvent.userId();
            userIdExist = true;
        } else {
            for(BotMessage botMessage:operatorEvent.messages()){
                if(botMessage.messageType() == MessageContentType.MENTION){
                    userId = Long.valueOf(botMessage.data());
                    userIdExist = true; 
                }
            }
        }
        if(!userIdExist){
            LOG.warn("userId not specified");
            sendGroupMessageWithMention(groupId, operatorId, "user not specified", -1);
            return;
        }
//        BotMessage textMessage = findTextMessage(trait, operatorEvent.messages());
//        if(textMessage != null){
//            cause = textMessage.data().replace(trait, "").trim();
//        }
        kickMember(groupId, userId, rejectJoin);
//        sendGroupMessage(groupId, "skymas admin kicks member "+userId, -1);
        StringBuilder sb = new StringBuilder();
        sb.append("user ").append(userId).append(" has been kicked");
        if(global){
            sb.append(" and global ban");
        }
        if(cause != null && !cause.isBlank()){
            sb.append(" because of ").append(cause);
        }
        sendGroupMessageWithMention(groupId, operatorId, sb.toString(), -1);
        if(originEvent != null){
            banMember(originEvent.messages(), groupId, userId, operatorId, CauseType.MANUAL, cause, global);
        } else {
            banMember(null, groupId, userId, operatorId, CauseType.MANUAL, cause, global);
        }
    }
    
    private BotMessage findTextMessage(String trait, BotMessage[] array){
        if(array == null){
            return null;
        }
        if(array[0].messageType() == MessageContentType.REPLY){
            int lastIndex = array.length-1;
            for(int i=1; i<array.length; i++){
                if(array[i].messageType() != MessageContentType.MENTION){
                    if(array[i].messageType() == MessageContentType.TEXT){
                        if(!array[i].data().trim().isBlank()){
                            if(trait.equals(array[i].data().trim())){
                                continue;
                            }
                            return array[i];
                        }
                    }else if(i == lastIndex){
                        return null;
                    }
                }
            }
        }
        for (BotMessage message : array) {
            if(message.messageType() == MessageContentType.TEXT){
                if(trait.equals(message.data().trim())){
                    continue;
                }
                return message;
            }
        }
        return null;
    }
    
    private void readDatabase(){
        String selectHql = "FROM UserBlockRecord";     
        try(Session session = sessionManager.openSession();){
            Query<UserBlockRecord> blockedQuery = session.createQuery(selectHql);
            List<UserBlockRecord> blockedList = blockedQuery.getResultList();
            
            Transaction tx = session.beginTransaction();
            long timestamp = Instant.now().toEpochMilli();
            blockedList.forEach(item -> {
                long diffMillis = item.getCreatetime().toEpochMilli()+(item.getDuration()*1000) - timestamp;
                if(diffMillis <= 0){
                    session.delete(item);
                    return;
                }
                String uuid = generateUuid(item.getGroupId(), item.getUserId());
                long timerId = vertx().setTimer(diffMillis, time -> {
                    blockedUsers.remove(uuid);
                    blockedUserTimers.remove(uuid);
                });
                blockedUsers.add(uuid);
                blockedUserTimers.put(uuid, timerId);
            });
            tx.commit();
            MDC.put("module", PLUGIN_NAME);
            LOG.info("{} bolcked user loaded", blockedUsers.size());
        }
    }
    
    private void configure() throws IOException {
        String file = path+"/config.yml";
        Configuration config = mapper.readValue(Files.readString(new File(file).toPath(), StandardCharsets.UTF_8), new TypeReference<Configuration>() {});
        if(config.isEnableDatabase()){
            
            Map<String, Object> settings = new HashMap<>();
//            settings.put(Environment.HBM2DDL_AUTO, "validate");   // validate the schema, makes no changes to the database.
    //        settings.put(Environment.HBM2DDL_AUTO, "create");   //creates the schema, destroying previous data.
            settings.put(Environment.VALIDATE_QUERY_PARAMETERS, "SELECT 1");
            settings.put(Environment.SHOW_SQL, true);
            settings.put(Environment.DEFAULT_SCHEMA, "group_manager");

            // HikariCP settings

            // Maximum waiting time for a connection from the pool
            settings.put("hibernate.hikari.connectionTimeout", "20000");
            // Minimum number of ideal connections in the pool
            settings.put("hibernate.hikari.minimumIdle", "10");
            // Maximum number of actual connection in the pool
            settings.put("hibernate.hikari.maximumPoolSize", "20");
            // Maximum time that a connection is allowed to sit ideal in the pool
            settings.put("hibernate.hikari.idleTimeout", "300000");
            settings.put("hibernate.hikari.autoCommit", "false");
            settings.put("hibernate.hikari.poolName", "Haruno Group Manager DB Pool");


            Class[] annotatedClasses = new Class[]{GroupBanRecord.class, UserBanRecord.class, UserBlockRecord.class, UserWarnRecord.class};
            sessionManager = new SessionManager(config.getDatabase());
            sessionManager.init(settings, annotatedClasses);
            readDatabase();
        }
        
        if(config.getSuperAdmins()==null || config.getSuperAdmins().isEmpty()){
            throw new IllegalArgumentException("super administor can't not be empty");
        }
        superAdmin.addAll(config.getSuperAdmins());
        
        if(config.getManualPenalty() != null){
            ManualPenaltyConfig manualPenaltyConfig = config.getManualPenalty();
            if(!manualPenaltyConfig.isGlobal() && (manualPenaltyConfig.getGroups() == null || manualPenaltyConfig.getGroups().isEmpty())){
                throw new IllegalArgumentException("manual penalty config  not global and group is empty");
            }
            if(manualPenaltyConfig.getCommandSplitChar() == null){
                throw new IllegalArgumentException("manual penalty command_split_char can not be null");
            }
            if(manualPenaltyConfig.getCommandSplitChar().length() != 1){
                throw new IllegalArgumentException("manual penalty command_split_char's length must be 1");
            }
            manualPenaltyWrapper = new ManualPenaltyWrapper(manualPenaltyConfig);
        }
        if(config.getPenaltyFiles()!=null && !config.getPenaltyFiles().isEmpty()){
            config.getPenaltyFiles().forEach(penaltyFile -> {
                try{
                    readAutoPenaltyFile(penaltyFile);
                    LOG.info("load penalty: {}", penaltyFile.getId());
                } catch (Exception ex) {
                    MDC.put("module", PLUGIN_NAME);
                    LOG.error("read auto penalty file {} failed.", penaltyFile.getFile(), ex);
                    MDC.clear();
                }
            });
        }
        if(config.getGlobalPenaltyConfigs()!=null && !config.getGlobalPenaltyConfigs().isEmpty()){
            config.getGlobalPenaltyConfigs().forEach(globalConfig -> {
                Set<Long> admins;
                if(globalConfig.getAdmins()!= null && !globalConfig.getAdmins().isEmpty()){
                    admins = new HashSet(globalConfig.getAdmins());
                } else {
                    admins = null;
                }
                globalConfig.getPenalties().forEach(globalPenalty -> {
                    globalPenalty.getIds().forEach(id -> {
                        if(!autoPenalties.containsKey(id)){
                            throw new IllegalArgumentException("auto penalty config "+id+" doesn't exist");
                        }
                        globalMatcheres.add(new GroupAutoPenaltyWrapper(id, globalPenalty, admins, autoPenalties.get(id).penalties()));
                    });
                });
            });
        }
        if(config.getGroupPenaltyConfigs()!=null && !config.getGroupPenaltyConfigs().isEmpty()){
            config.getGroupPenaltyConfigs().forEach(groupConfig -> {
                long groupId = groupConfig.getGroupId();
                Set<Long> admins;
                if(groupConfig.getAdmins()!= null && !groupConfig.getAdmins().isEmpty()){
                    admins = new HashSet(groupConfig.getAdmins());
                } else {
                    admins = null;
                }
                List<GroupAutoPenaltyWrapper> groupAutoPenaltyWrappers = new ArrayList();
                groupConfig.getPenalties().forEach(groupPenalty -> {
                    groupPenalty.getIds().forEach(id -> {
                        if(!autoPenalties.containsKey(id)){
                            throw new IllegalArgumentException("auto penalty config "+id+" doesn't exist");
                        }
                        groupAutoPenaltyWrappers.add(new GroupAutoPenaltyWrapper(id, groupPenalty, admins, autoPenalties.get(id).penalties()));
                    });
                });
                groupMatcheres.put(groupId, groupAutoPenaltyWrappers);
                if(groupConfig.getTurnover() != null && groupConfig.getTurnover().isEnable()){
                    groupTurnover.put(groupId, new TurnoverWrapper(groupId, groupConfig.getTurnover()));
                }
            });
        }
    }
    
    private void readAutoPenaltyFile(AutoPenaltyFileConfig config) throws IOException{
        String file = new StringBuilder()
                .append(path)
                .append("/")
                .append(config.getFile())
                .toString();
        List<AutoPenaltyMatcherConfig> matcherConfigs = mapper.readValue(Files.readString(new File(file).toPath(), StandardCharsets.UTF_8), new TypeReference<List<AutoPenaltyMatcherConfig>>() {});
        List<AutoPenaltyMatcherWrapper> matcherWrappers = new ArrayList(matcherConfigs.size());
        matcherConfigs.forEach((var matcherConfig) -> {
            matcherWrappers.add(new AutoPenaltyMatcherWrapper(matcherConfig));
//            if(config.isGlobal()){
//                globalMatcheres.add(matcherConfigWrapper);
//            } else if(config.getGroups() != null && !config.getGroups().isEmpty()){
//                config.getGroups().stream().map(groupId -> {
//                    if(!groupMatcheres.containsKey(groupId)){
//                        groupMatcheres.put(groupId, new ArrayList());
//                    }
//                    return groupId;
//                }).forEachOrdered(groupId -> {
//                    groupMatcheres.get(groupId).add(matcherConfigWrapper);
//                });
//            } else {
//                throw new IllegalArgumentException("auto penalty config "+config.getFile()+" not global and group is empty");
//            }
        });
        autoPenalties.put(config.getId(), new AutoPenaltyWrapper(config.getId(), matcherWrappers));
    }

    @Override
    public boolean onUnload() {
        enable = false;
        blockedUsers.clear();
        groupTurnover.clear();
        autoPenalties.clear();
        globalMatcheres.clear();
        groupMatcheres.clear();
        try {
            sessionManager.close();
        } catch (Exception ex) {
            MDC.put("module", PLUGIN_NAME);
            LOG.error("session manager close failed", ex);
            MDC.clear();
        }
        return true;
    }

    @Override
    public boolean onEnable() {
        enable = true;
        return true;
    }
    
}
