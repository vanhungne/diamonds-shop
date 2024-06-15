package org.example.diamondshopsystem.services;

import org.example.diamondshopsystem.dto.DiamondDTO;
import org.example.diamondshopsystem.dto.DiamondPriceDTO;
import org.example.diamondshopsystem.entities.Diamond;
import org.example.diamondshopsystem.repositories.DiamondsRepository;
import org.example.diamondshopsystem.services.imp.DiamondPriceServiceImp;
import org.example.diamondshopsystem.services.imp.DiamondServiceImp;
import org.example.diamondshopsystem.services.imp.ProductServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class DiamondService implements DiamondServiceImp {
    @Autowired
    private DiamondsRepository diamondsRepository;

    @Autowired
    private DiamondPriceServiceImp diamondPriceService;

    @Autowired
    private ProductServiceImp productService;

    @Override
    public DiamondDTO createDiamond(DiamondDTO diamondDTO) {
        Diamond diamond = new Diamond();
        diamond.setCut(diamondDTO.getCut());
        diamond.setColor(diamondDTO.getColor());
        diamond.setClarity(diamondDTO.getClarity());
        diamond.setCarat(diamondDTO.getCarat());

        diamond.setPrice(diamondPriceService.getDiamondPriceBy4C(
                diamondDTO.getCarat(),
                diamondDTO.getColor(),
                diamondDTO.getCut(),
                diamondDTO.getClarity()).getPrice()
        );
        diamond.setCertification(diamondDTO.getCertification());
        diamond.setStatus(true);

        Diamond savedDiamond = diamondsRepository.save(diamond);
        return mapDiamondToDiamondDTO(savedDiamond);
    }

    @Override
    public DiamondDTO updateDiamond(DiamondDTO diamondDTO) {
        Diamond diamond = diamondsRepository.findById(diamondDTO.getDiamondId())
                .orElseThrow(() -> new NoSuchElementException("Cannot found diamond with id: " + diamondDTO.getDiamondId()));
        diamond.setCut(diamondDTO.getCut());
        diamond.setColor(diamondDTO.getColor());
        diamond.setClarity(diamondDTO.getClarity());
        diamond.setCarat(diamondDTO.getCarat());
        diamond.setCertification(diamondDTO.getCertification());
        diamond.setProduct(productService.getProductById(diamondDTO.getProductId()));

        diamond.setPrice(diamondPriceService.getDiamondPriceBy4C(
                diamondDTO.getCarat(),
                diamondDTO.getColor(),
                diamondDTO.getCut(),
                diamondDTO.getClarity()).getPrice()
        );
        diamond.setStatus(diamondDTO.isStatus());

        Diamond savedDiamond = diamondsRepository.save(diamond);
        return mapDiamondToDiamondDTO(savedDiamond);
    }



    @Override
    public DiamondDTO deleteDiamond(int id) {
        Diamond diamond = diamondsRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Cannot found diamond with id: " + id));

        diamond.setPrice(0);
        productService.updateProductPrice(diamond);
        diamond.setStatus(false);
        diamondsRepository.save(diamond); // xóa thìdoi status thoi
        return mapDiamondToDiamondDTO(diamond);
    }

    @Override
    public List<DiamondDTO> getAllDiamonds() {
        List<Diamond> diamonds = diamondsRepository.findAll();
        List<DiamondDTO> diamondDTOs = new ArrayList<>();
        for (Diamond diamond : diamonds) {
            diamondDTOs.add(mapDiamondToDiamondDTO(diamond));
        }
        return diamondDTOs;
    }

    @Override
    public DiamondDTO getDiamondById(int id) {
        Diamond diamond = diamondsRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Cannot found diamond with id: " + id));
        return mapDiamondToDiamondDTO(diamond);
    }

    @Override
    public List<DiamondDTO> getDiamondsBy4C(DiamondDTO diamondDTO) {
        double carat = diamondDTO.getCarat();
        String color = diamondDTO.getColor();
        String clarity = diamondDTO.getClarity();
        String cut = diamondDTO.getCut();

        List<Diamond> diamondList = diamondsRepository.findAllByCaratAndCutAndColorAndClarity(carat, cut, color, clarity);
        List<DiamondDTO> diamondDTOs = new ArrayList<>();
        for (Diamond diamond : diamondList) {
            diamondDTOs.add(mapDiamondToDiamondDTO(diamond));
        }
        return diamondDTOs;
    }

    @Override
    public void updateDiamondPrice(DiamondPriceDTO diamondPriceDTO) {
        double carat = diamondPriceDTO.getCarat();
        String color = diamondPriceDTO.getColor();
        String clarity = diamondPriceDTO.getClarity();
        String cut = diamondPriceDTO.getCut();
        List<Diamond> diamonds = diamondsRepository.findAllByCaratAndCutAndColorAndClarity(carat, cut, color, clarity);
        for (Diamond diamond : diamonds) {
            diamond.setPrice(diamondPriceDTO.getPrice());
            diamondsRepository.save(diamond);
            productService.updateProductPrice(diamond);
        }
    }

    @Override
    public DiamondDTO getDiamondByProductId(int productId) {
        Diamond diamond = diamondsRepository.findFirstByProduct_ProductIdAndStatusIsTrue(productId);
        if(diamond != null ) {
            return mapDiamondToDiamondDTO(diamond);
        }else {
            throw new NoSuchElementException("Cannot found diamond with id: " + productId);
        }
    }

    DiamondDTO mapDiamondToDiamondDTO(Diamond diamond) {
        DiamondDTO diamondDTO = new DiamondDTO();
        diamondDTO.setDiamondId(diamond.getDiamondId());
        diamondDTO.setCut(diamond.getCut());
        diamondDTO.setClarity(diamond.getClarity());
        diamondDTO.setColor(diamond.getColor());
        diamondDTO.setCarat(diamond.getCarat());
        diamondDTO.setPrice(diamond.getPrice());
        diamondDTO.setCertification(diamond.getCertification());
        diamondDTO.setProductId(diamond.getProduct().getProductId());
        diamondDTO.setStatus(diamond.isStatus());
        return diamondDTO;
    }
}
