package ru.pankkovv.auctioneerBot.enums;

public enum CommandMessage {
    START("Давайте начнём \n\n" +
            "Я бот-аукционер, рад знакомству! \n" +
            "Моей задачей является оказание помощи в проведении различных аукционов."),
    HELP("Если возникнут вопросы по работе с ботом\n" +
            "или же предложения и комментарии, вы можете нам написать:\n" +
            "@pankkovv - создатель и администратор бота\n" +
            "@eee_kisel - заказчик бота\n"),
    LOT("Актуальная информация по лоту:\n"),
    BET("Чтобы сделать новую ставку напишите мне цену,\n" +
            "которую вы готовы предложить за лот.\n" +
            "Условия для принятия вашей ставки:\n" +
            "1. Ваша цена не может быть меньше актуальной стоимости лота\n" +
            "2. Увеличение цены лота не может быть меньше установленного шага торгов\n" +
            "3. Увеличение цены лота должно быть кратно установленному шагу торгов\n\n" +
            "Я понимаю только сообщения в формате: \"1600\""),
    CANCEL("Ваша ставка отменена."),
    REGISTRATION("Регистрация прошла успешно, рад знакомству %s\uD83E\uDD1D\n" +
            "Теперь вам доступны права администратора."),
    CREATE("Для регистрации лота необходимо отправить мне следующие данные:\n" +
            "1. Наименование лота\n" +
            "2. Начальная стоимость\n" +
            "3. Шаг ставки\n\n" +
            "Я понимаю только сообщения в формате: \"кольцо 1500 100\"\n" +
            "По желанию, можно добавить одно изображение для лота.\n" +
            "Желаю успешных торгов\uD83E\uDD17"),
    UPDATE("Для обновления текстовых данных лота необходимо отправить мне следующие данные:\n" +
            "1. Наименование лота\n" +
            "2. Начальная стоимость\n" +
            "3. Шаг ставки\n\n" +
            "Я понимаю только сообщения в формате: \"кольцо 1500 100\"\n" +
            "Для обновления изображения лота необходимо отправить мне пустое сообщение с новым фото во вложении.\n" +
            "Вы также можете обновить все данные вместе, для этого воспользуйтесь инструкцией команды /create\n\n" +
            "Желаю успешных торгов \uD83E\uDD17"),
    DELETE("Лот успешно удален!"),

    TABLE("Таблица (ник ставка):\n"),
    TRY_CREATE_LOT("Лот успешно создан(обновлен)!"),
    TRY_BET("Ваша ставка %s успешно принята!");

    public final String label;

    CommandMessage(String label) {
        this.label = label;
    }
}