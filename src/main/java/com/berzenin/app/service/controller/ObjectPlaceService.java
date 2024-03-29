package com.berzenin.app.service.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.berzenin.app.dao.ObjectPlaceRepository;
import com.berzenin.app.model.Merch;
import com.berzenin.app.model.ObjectPlace;
import com.berzenin.app.model.Photo;

@Service
public class ObjectPlaceService extends GenericServiceImpl<ObjectPlace, ObjectPlaceRepository> {

	public ObjectPlaceService(ObjectPlaceRepository repository) {
		super(repository);
	}	
	
	@Autowired
	private PhotoService photoService;
	
	@Autowired
	private MerchService merchService;	

	public Map<LocalDate, Map<Merch, List<Photo>>> getPhotosByDates(ObjectPlace shops, List<LocalDate> dates) {
		Map<LocalDate, Map<Merch, List<Photo>>> photoBydates = new HashMap<>();
		
		for(LocalDate date: dates) {
			Map<Merch, List<Photo>> photoByObject = shops.getPhotos().stream()
				.filter(photo -> photo.getDate().equals(date))
				.collect(Collectors.groupingBy(Photo::getMerch,
				   Collectors.mapping(
				       photo -> photoService.findById(photo.getId()), Collectors.toList())));
			
			photoBydates.put(date, photoByObject);
		}
		for (LocalDate date: dates) {
			if (!photoBydates.containsKey(date)) {
				photoBydates.put(date, null);
			}
		}
		SortedMap sortedMap = new TreeMap();
		sortedMap.putAll(photoBydates);
		return sortedMap;
	}

	public Optional<ObjectPlace> findByName(String name) {
		return repository.findByName(name);
	}
	
	@Override
	public void removeById(Long id) {
		ObjectPlace entity = this.findById(id);
		ObjectPlace arhiv;
		if (this.findByName("arhiv").isPresent()) {
			arhiv = this.findByName("arhiv").get();
		} else {
			arhiv = new ObjectPlace();
			arhiv.setName("arhiv");
			this.add(arhiv);
		}
		entity.getPhotos().forEach(photo -> {
			photo.setObjectPlace(arhiv);
			photoService.update(photo);
		});
		entity.getMerch().forEach(merch -> {
			merch.getObjectPlace().remove(entity);
			merchService.update(merch);
		});
		entity.setMerch(null);
		this.update(entity);
		super.remove(entity);
	}
}
