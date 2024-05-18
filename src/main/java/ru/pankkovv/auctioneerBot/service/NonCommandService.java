package ru.pankkovv.auctioneerBot.service;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import ru.pankkovv.auctioneerBot.enums.ButtonData;
import ru.pankkovv.auctioneerBot.enums.Command;
import ru.pankkovv.auctioneerBot.enums.CommandMessage;
import ru.pankkovv.auctioneerBot.enums.ExceptionMessage;
import ru.pankkovv.auctioneerBot.exception.AdminNotFoundException;
import ru.pankkovv.auctioneerBot.exception.BetException;
import ru.pankkovv.auctioneerBot.exception.LotNotFoundException;
import ru.pankkovv.auctioneerBot.model.Button;
import ru.pankkovv.auctioneerBot.model.Lot;
import ru.pankkovv.auctioneerBot.utils.Utils;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.util.Comparator;

import static ru.pankkovv.auctioneerBot.model.Auction.*;

/**
 * Обработка сообщения, не являющегося командой (т.е. обычного текста не начинающегося с "/")
 */
@Slf4j
public class NonCommandService {
    public SendPhoto nonCommandExecute(Long chatId, String userName, String text) {
        String answer;
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setReplyMarkup(Button.getStartButton());

        String[] parameters = text.replaceAll("-", "")//избавляемся от отрицательных чисел
                .replaceAll("/", "")//меняем ошибочный разделитель "запятая+пробел" на запятую
                .replaceAll(", ", ",")//меняем ошибочный разделитель "запятая+пробел" на запятую
                .replaceAll(" ", ",")//меняем разделитель-пробел на запятую
                .split(",");

        if (parameters.length == 1) {
            if (Command.START.label.equals(parameters[0])) {
                answer = CommandMessage.START.label;
                sendPhoto.setPhoto(new InputFile(new File("imgBtn/start.jpg")));
            } else {
                try (FileWriter fileWriter = new FileWriter("bidding.csv", true)) {
                    Utils.containsLot();
                    Float bet = validBet(parameters[0]);

                    if (flag) {
                        fileWriter.append("time;username;bet\n");
                        flag = false;
                    }

                    bidding.put(userName, bet);
                    lot.setCurrentPrice(bet);
                    fileWriter.append(String.format("%s;%s;%s\n", LocalDateTime.now(), userName, bet));

                    answer = String.format(CommandMessage.TRY_BET.label, bet);
                    sendPhoto.setReplyMarkup(Button.getBetButton());
                    sendPhoto.setPhoto(new InputFile(new File("imgBtn/bet.jpg")));

                } catch (BetException e) {
                    answer = e.getMessage();
                    sendPhoto.setPhoto(new InputFile(new File("imgBtn/betExc.jpg")));
                } catch (LotNotFoundException e) {
                    answer = e.getMessage();
                    sendPhoto.setPhoto(new InputFile(new File("imgBtn/notFoundLot.jpg")));
                } catch (Exception e) {
                    answer = ExceptionMessage.NOT_FOUND_COMMAND_EXCEPTION.label;
                    sendPhoto.setPhoto(new InputFile(new File("imgBtn/tgEr.jpg")));
                }
            }
        } else if (parameters.length == 3) {
            try {
                Utils.containsAdmin(userName);
                lot = validLot(parameters, null);

                answer = CommandMessage.TRY_CREATE_LOT.label;
                sendPhoto.setReplyMarkup(Button.getLotButton());
                sendPhoto.setPhoto(new InputFile(new File("imgBtn/create.jpg")));
            } catch (AdminNotFoundException e) {
                answer = e.getMessage();
                sendPhoto.setPhoto(new InputFile(new File("imgBtn/notRulesAdmin.jpg")));
            }
        } else {
            answer = ExceptionMessage.NOT_FOUND_COMMAND_EXCEPTION.label;
            sendPhoto.setPhoto(new InputFile(new File("imgBtn/tgEr.jpg")));
        }

        sendPhoto.setChatId(chatId.toString());
        sendPhoto.setCaption(answer);

        if (admin.contains(userName)) {
            sendPhoto.setReplyMarkup(Button.getAllButton());
        }

        return sendPhoto;
    }

    public Object nonCommandExecute(Long chatId, String userName, CallbackQuery cbq) {
        log.debug(String.format("Начата обработка сообщения \"%s\", не являющегося командой", cbq.getMessage().getText()));

        SendPhoto sendPhoto = new SendPhoto();

        String button = ButtonData.valueOf(cbq.getData().toUpperCase()).label;
        String text;

        switch (button) {
            case "start_btn":
                text = CommandMessage.START.label;
                sendPhoto.setCaption(text);
                sendPhoto.setPhoto(new InputFile(new File("imgBtn/start.jpg")));
                sendPhoto.setReplyMarkup(Button.getStartButton());
                break;

            case "help_btn":
                text = CommandMessage.HELP.label;

                sendPhoto.setCaption(text);
                sendPhoto.setPhoto(new InputFile(new File("imgBtn/help.jpg")));
                sendPhoto.setReplyMarkup(Button.getStartButton());

                sendPhoto.setReplyMarkup(Button.getHelpButton());
                break;

            case "lot_btn":
                try {
                    Utils.containsLot();
                    text = CommandMessage.LOT.label + lot;
                    sendPhoto.setPhoto(new InputFile(new File("imgBtn/lot.jpg")));

                    if (lot.getPhoto() != null) {
                        sendPhoto.setPhoto(new InputFile(lot.getPhoto()));
                    }
                } catch (LotNotFoundException e) {
                    text = e.getMessage();
                    sendPhoto.setPhoto(new InputFile(new File("imgBtn/notFoundLot.jpg")));
                }

                sendPhoto.setCaption(text);
                sendPhoto.setReplyMarkup(Button.getLotButton());
                break;

            case "bet_btn":
                text = CommandMessage.BET.label;
                sendPhoto.setCaption(text);
                sendPhoto.setPhoto(new InputFile(new File("imgBtn/bet.jpg")));
                sendPhoto.setReplyMarkup(Button.getBetButton());
                break;

            case "cancel_btn":
                try {
                    Utils.containsLot();
                    Utils.containsBet(userName);
                    text = CommandMessage.CANCEL.label;

                    bidding.remove(userName);
                    lot.setCurrentPrice(bidding.values().stream()
                            .max(Comparator.comparing(Float::valueOf))
                            .orElse(lot.getStartPrice()));

                    sendPhoto.setCaption(text);
                    sendPhoto.setPhoto(new InputFile(new File("imgBtn/cancel.jpg")));
                } catch (LotNotFoundException e) {
                    text = e.getMessage();
                    sendPhoto.setCaption(text);
                    sendPhoto.setPhoto(new InputFile(new File("imgBtn/notFoundLot.jpg")));
                } catch (BetException e) {
                    text = e.getMessage();
                    sendPhoto.setCaption(text);
                    sendPhoto.setPhoto(new InputFile(new File("imgBtn/betExc.jpg")));
                }

                sendPhoto.setReplyMarkup(Button.getCancelButton());

                break;

            case "registration_btn":
                text = CommandMessage.REGISTRATION.label;
                sendPhoto.setCaption(text);
                sendPhoto.setReplyMarkup(Button.getAllButton());
                break;

            case "create_btn":
                text = CommandMessage.CREATE.label;
                sendPhoto.setCaption(text);
                sendPhoto.setPhoto(new InputFile(new File("imgBtn/create.jpg")));
                sendPhoto.setReplyMarkup(Button.getStartButton());
                break;

            case "update_btn":
                text = CommandMessage.UPDATE.label;
                sendPhoto.setCaption(text);
                sendPhoto.setPhoto(new InputFile(new File("imgBtn/update.jpg")));
                sendPhoto.setReplyMarkup(Button.getStartButton());
                break;

            case "delete_btn":
                text = CommandMessage.DELETE.label;
                sendPhoto.setCaption(text);
                sendPhoto.setPhoto(new InputFile(new File("imgBtn/delete.jpg")));
                sendPhoto.setReplyMarkup(Button.getStartButton());
                break;

            case "table_btn":
                text = CommandMessage.TABLE.label;
                sendPhoto.setCaption(text);
                sendPhoto.setPhoto(new InputFile(new File("imgBtn/table.jpg")));
                sendPhoto.setReplyMarkup(Button.getStartButton());
                break;
        }

        sendPhoto.setChatId(String.valueOf(chatId));

        if (admin.contains(userName)) {
            sendPhoto.setReplyMarkup(Button.getAllButton());
        }

        return sendPhoto;
    }

    public SendPhoto nonCommandExecute(Long chatId, String userName, String text, File photo) {
        String answer;
        SendPhoto sendPhoto = new SendPhoto();

        String[] parameters = text.replaceAll("-", "")//избавляемся от отрицательных чисел
                .replaceAll("/", "")//меняем ошибочный разделитель "запятая+пробел" на запятую
                .replaceAll(", ", ",")//меняем ошибочный разделитель "запятая+пробел" на запятую
                .replaceAll(" ", ",")//меняем разделитель-пробел на запятую
                .split(",");

        if (!text.isEmpty()) {
            lot = validLot(parameters, photo);
        } else {
            lot.setPhoto(photo);
        }

        answer = CommandMessage.TRY_CREATE_LOT.label;

        sendPhoto.setChatId(String.valueOf(chatId));
        sendPhoto.setCaption(answer);
        sendPhoto.setReplyMarkup(Button.getStartButton());
        sendPhoto.setPhoto(new InputFile(new File("imgBtn/create.jpg")));

        if (admin.contains(userName)) {
            sendPhoto.setReplyMarkup(Button.getAllButton());
        }

        return sendPhoto;
    }

    private Lot validLot(String[] parameters, File photo) {
        String name = parameters[0];
        Float startPrice = Float.parseFloat(parameters[1]);
        Float step = Float.parseFloat(parameters[2]);

        return Lot.builder()
                .photo(photo)
                .name(name)
                .startPrice(startPrice)
                .currentPrice(startPrice)
                .step(step)
                .build();
    }

    private Float validBet(String parameter) {
        Float bet = Float.parseFloat(parameter);
        Float maxPrice = 0.0F;

        for (String name : bidding.keySet()) {
            if (bidding.get(name) > maxPrice) {
                maxPrice = bidding.get(name);
            }
        }

        if (bet <= lot.getCurrentPrice() ||
                bet.equals(maxPrice) ||
                (bet - maxPrice) < lot.getStep() ||
                (bet - maxPrice) % lot.getStep() != 0) {
            throw new BetException(ExceptionMessage.VALIDATE_BET_EXCEPTION.label);
        }

        return bet;
    }
}