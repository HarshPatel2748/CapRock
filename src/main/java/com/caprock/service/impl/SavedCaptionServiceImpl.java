package com.caprock.service.impl;

import com.caprock.dto.SavedCaptionRequest;
import com.caprock.dto.SavedCaptionResponse;
import com.caprock.model.SavedCaption;
import com.caprock.model.User;
import com.caprock.repository.SavedCaptionRepository;
import com.caprock.repository.UserRepository;
import com.caprock.service.SavedCaptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class SavedCaptionServiceImpl implements SavedCaptionService {

    @Autowired
    private SavedCaptionRepository savedCaptionRepository;

    @Autowired
    private UserRepository userRepository;

    //Helper
    private User getUser(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found."
                ));
    }

    //Get All Captions
    @Override
    public List<SavedCaptionResponse> getAllSaved(String userEmail){
        User user = getUser(userEmail);

        return savedCaptionRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(SavedCaptionResponse::new)
                .toList();
    }

    //Save the caption
    @Override
    public SavedCaptionResponse saveCaption(SavedCaptionRequest request, String userEmail){

        User user = getUser(userEmail);

        //Free plan user's can't save captions
        if(user.getPlan().equals("free")){
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Saving captions is not available on the free plan. Please upgrade."
            );
        }

        SavedCaption caption = SavedCaption.builder()
                .user(user)
                .captionText(request.getCaptionText())
                .hashtags(request.getHashtags())
                .style(request.getStyle())
                .platform(request.getPlatform())
                .build();

        savedCaptionRepository.save(caption);

        return new SavedCaptionResponse(caption);
    }

    //Delete the caption
    @Override
    public void deleteCaption(Long captionId, String userEmail){
        User user = getUser(userEmail);

        SavedCaption caption = savedCaptionRepository.findById(captionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Saved caption not found"
                ));

        //Make sure this caption belongs to the logged-in user
        if(!caption.getUser().getId().equals(user.getId())){
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You do not have permission to delete this caption"
            );
        }

        savedCaptionRepository.delete(caption);
    }
}
