package org.hcmu.hcmuserver.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hcmu.hcmupojo.dto.AiAssistantDTO;
import org.hcmu.hcmupojo.entity.Department;
import org.hcmu.hcmuserver.config.DoctorAssistantProperties;
import org.hcmu.hcmuserver.mapper.department.DepartmentMapper;
import org.hcmu.hcmuserver.service.DoctorAssistantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.github.yulichang.wrapper.MPJLambdaWrapper;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DoctorAssistantServiceImpl implements DoctorAssistantService {

    private final RestTemplate aiAssistantRestTemplate;
    private final DoctorAssistantProperties doctorAssistantProperties;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Override
    public AiAssistantDTO.ChatCompletionResponse chat(AiAssistantDTO.ChatCompletionRequest request) {
        if (request == null || CollectionUtils.isEmpty(request.getMessages())) {
            throw new IllegalArgumentException("messages不能为空");
        }

        if (Boolean.TRUE.equals(request.getStream())) {
            throw new IllegalArgumentException("当前接口暂不支持流式响应");
        }

        AiAssistantDTO.ChatCompletionRequest upstreamRequest = buildUpstreamRequest(request);
        HttpHeaders headers = buildHeaders();

        String url = buildChatUrl();

        HttpEntity<AiAssistantDTO.ChatCompletionRequest> entity = new HttpEntity<>(upstreamRequest, headers);

        try {
            ResponseEntity<AiAssistantDTO.ChatCompletionResponse> response = aiAssistantRestTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<AiAssistantDTO.ChatCompletionResponse>() {
                    }
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("调用智能医生助手失败，status={}", response.getStatusCode());
                throw new RestClientException("智能医生助手服务调用失败，状态码：" + response.getStatusCode());
            }

            AiAssistantDTO.ChatCompletionResponse responseBody = response.getBody();
            if (responseBody == null) {
                log.warn("智能医生助手返回空响应");
                throw new RestClientException("智能医生助手服务返回空响应");
            }

            return responseBody;
        } catch (RestClientException ex) {
            log.error("调用智能医生助手异常", ex);
            throw ex;
        }
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(doctorAssistantProperties.getApiKey());
        return headers;
    }

    private String buildChatUrl() {
        String baseUrl = doctorAssistantProperties.getBaseUrl();
        String chatPath = doctorAssistantProperties.getChatPath();

        if (!StringUtils.hasText(chatPath)) {
            chatPath = "/v1/chat/completions";
        }

        if (baseUrl.endsWith("/") && chatPath.startsWith("/")) {
            chatPath = chatPath.substring(1);
        } else if (!baseUrl.endsWith("/") && !chatPath.startsWith("/")) {
            chatPath = "/" + chatPath;
        }

        return UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path(chatPath)
                .toUriString();
    }

    private AiAssistantDTO.ChatCompletionRequest buildUpstreamRequest(AiAssistantDTO.ChatCompletionRequest request) {
        AiAssistantDTO.ChatCompletionRequest upstream = new AiAssistantDTO.ChatCompletionRequest();
        upstream.setModel(doctorAssistantProperties.getModel());
        upstream.setTemperature(request.getTemperature());
        upstream.setTopP(request.getTopP());
        upstream.setMaxTokens(request.getMaxTokens());
        upstream.setStream(false);

        List<AiAssistantDTO.ChatMessage> messages = new ArrayList<>();
        String prompt = "你是基米医生，可以帮助患者解答医疗相关的问题，尤其是帮助患者进行预诊并进行就诊科室的推荐。请确保你的回答准确且专业。\n目前医院的科室有：\n";

        MPJLambdaWrapper<Department> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.selectAll(Department.class)
                .ne(Department::getDepartmentId, 0);
        List<Department> departments = departmentMapper.selectList(queryWrapper);
        for (Department dept : departments) {
            if (dept.getParentId() == null || dept.getParentId() == 0) {
                continue;
            }
            prompt += String.format("科室ID(departmentId)：%d，科室名称：%s；，父科室ID(parentId)：%d\n", dept.getDepartmentId(), dept.getName(), dept.getParentId());
        }

        prompt += "当推荐科室时，需要你解释一下可能的原因，可能的疾病类型等。如果用户描述的症状不明确，可以先给出可能的结果，再通过提问获取更多信息帮用户定位。\n";

        prompt += "你只需要以“[科室名](/pages/appointment/dep/dep?parentId={父科室ID}&departmentId={科室ID})”的格式来推荐科室，用户就能点击转跳到对应页面进行预约挂号。\n";



        if (StringUtils.hasText(prompt)) {
            AiAssistantDTO.ChatMessage systemMessage = new AiAssistantDTO.ChatMessage();
            systemMessage.setRole("system");
            systemMessage.setContent(prompt);
            messages.add(systemMessage);
        }

        messages.addAll(request.getMessages());
        upstream.setMessages(messages);
        return upstream;
    }
}
