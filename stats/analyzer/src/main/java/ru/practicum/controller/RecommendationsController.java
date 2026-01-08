package ru.practicum.controller;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.proto.*;
import ru.practicum.service.RecommendationsService;

import java.util.List;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class RecommendationsController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final RecommendationsService recommendationsService;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        log.info("RecommendationsControllerGrpc invoked with method getRecommendationsForUser(), request={}", request);
        try {
            List<RecommendedEventProto> recommendations = recommendationsService.getRecommendationsForUser(request);
            recommendations.forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error while getting recommendations for user, userId={}, maxResult={}", request.getUserId(), request.getMaxResult());
            log.error("Error message: {}", e.getMessage());
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        log.info("RecommendationsControllerGrpc invoked with method getSimilarEvents(), request={}", request);

        try {
            List<RecommendedEventProto> recommendations = recommendationsService.getSimilarEvent(request);
            recommendations.forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error while getting similar for user, userId={}, eventId={} ,maxResult={}",
                    request.getUserId(), request.getEventId(), request.getMaxResult());
            log.error("Error message: {}", e.getMessage());
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        log.info("getInteractionsCount invoked with method getSimilarEvents(), request={}", request);
        List<Long> eventIdList = request.getEventIdList();
        try {
            List<RecommendedEventProto> recommendations = recommendationsService.getInteractionsCount(request);

            if (recommendations == null || recommendations.isEmpty()) {
                eventIdList.forEach(id -> {
                    responseObserver.onNext(
                            RecommendedEventProto.newBuilder()
                                    .setEventId(id)
                                    .setScore(0.0)
                                    .build());
                });
                responseObserver.onCompleted();
                return;
            }

            recommendations.forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error while getting interactions count,  events size={}", eventIdList.size());
            log.error("Error message: {}", e.getMessage());
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }
}
