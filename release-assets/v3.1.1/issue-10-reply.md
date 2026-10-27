Thanks for reporting this! Existing filament names should stay unchanged unless you explicitly rename them.

I’ve fixed this in [v3.1.1](https://github.com/GeorgHo/SpoolStudio/releases/tag/v3.1.1): the app now preserves the full name and its original capitalization instead of replacing it with a color suggestion. The field is labeled **Filament Name**, and the previous 40-character input limit has been removed.

Could you please test the new version by updating an existing spool and checking that its name stays unchanged, including when you change the color? Please let me know if it works for you. This prevents further unwanted changes; previously overwritten names will need to be restored manually.

Thanks for your help!
