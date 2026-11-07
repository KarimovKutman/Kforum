/* Kforum frontend interactions. No external libraries are required. */
(() => {
    'use strict';

    const $ = (selector, root = document) => root.querySelector(selector);
    const $$ = (selector, root = document) => Array.from(root.querySelectorAll(selector));

    document.documentElement.classList.add('js-enabled');

    // Mobile navigation
    const navInner = $('.nav-inner');
    const mainNav = $('.main-nav');
    if (navInner && mainNav) {
        const toggle = document.createElement('button');
        toggle.type = 'button';
        toggle.className = 'nav-toggle';
        toggle.setAttribute('aria-controls', 'main-nav');
        toggle.setAttribute('aria-expanded', 'false');
        toggle.setAttribute('aria-label', 'Открыть меню');
        toggle.innerHTML = '<span></span><span></span><span></span>';
        mainNav.id = 'main-nav';
        navInner.insertBefore(toggle, mainNav);

        const closeMenu = () => {
            navInner.classList.remove('nav-open');
            toggle.setAttribute('aria-expanded', 'false');
            toggle.setAttribute('aria-label', 'Открыть меню');
        };
        toggle.addEventListener('click', () => {
            const open = navInner.classList.toggle('nav-open');
            toggle.setAttribute('aria-expanded', String(open));
            toggle.setAttribute('aria-label', open ? 'Закрыть меню' : 'Открыть меню');
        });
        mainNav.addEventListener('click', event => {
            if (event.target.closest('a')) closeMenu();
        });
        window.addEventListener('resize', () => {
            if (window.innerWidth > 680) closeMenu();
        });
    }

    // Mark the current navigation link.
    const currentPath = window.location.pathname.replace(/\/$/, '') || '/';
    $$('.main-nav a').forEach(link => {
        try {
            const linkPath = new URL(link.href, window.location.origin).pathname.replace(/\/$/, '') || '/';
            const isHome = linkPath === '/' && currentPath === '/';
            const isSection = linkPath !== '/' && (currentPath === linkPath || currentPath.startsWith(linkPath + '/'));
            if (isHome || isSection) {
                link.classList.add('is-active');
                link.setAttribute('aria-current', 'page');
            }
        } catch (_) {
            // Ignore malformed URLs rather than breaking the rest of the page scripts.
        }
    });

    // Flash messages become dismissible and fade away automatically.
    $$('.alert').forEach((alert, index) => {
        if (!alert.textContent.trim()) return;
        if (!alert.querySelector('.alert-dismiss')) {
            const close = document.createElement('button');
            close.type = 'button';
            close.className = 'alert-dismiss';
            close.setAttribute('aria-label', 'Закрыть уведомление');
            close.textContent = '×';
            close.addEventListener('click', () => dismissAlert(alert));
            alert.appendChild(close);
        }
        window.setTimeout(() => dismissAlert(alert), alert.classList.contains('alert-error') ? 9000 : 6500 + index * 250);
    });

    function dismissAlert(alert) {
        if (!alert || alert.dataset.dismissed === 'true') return;
        alert.dataset.dismissed = 'true';
        alert.classList.add('alert-hiding');
        window.setTimeout(() => alert.remove(), 260);
    }

    // Character counters for fields with a maxlength attribute.
    $$('input[maxlength], textarea[maxlength]').forEach(field => {
        if (field.type === 'hidden' || field.dataset.noCounter === 'true') return;
        const max = Number.parseInt(field.getAttribute('maxlength'), 10);
        if (!Number.isFinite(max) || max < 1) return;

        const counter = document.createElement('small');
        counter.className = 'char-counter';
        counter.setAttribute('aria-live', 'polite');
        const wrapper = field.closest('.form-field') || field.parentElement;
        if (wrapper) wrapper.appendChild(counter);

        const updateCounter = () => {
            const remaining = max - field.value.length;
            counter.textContent = `${field.value.length} / ${max}`;
            counter.classList.toggle('is-near-limit', remaining <= Math.max(10, Math.round(max * 0.1)));
            counter.classList.toggle('is-over-limit', remaining < 0);
        };
        field.addEventListener('input', updateCounter);
        updateCounter();
    });

    // Show/hide password buttons.
    $$('input[type="password"]').forEach(input => {
        const parent = input.parentElement;
        if (!parent) return;
        const wrapper = document.createElement('div');
        wrapper.className = 'password-input-wrap';
        parent.insertBefore(wrapper, input);
        wrapper.appendChild(input);

        const toggle = document.createElement('button');
        toggle.type = 'button';
        toggle.className = 'password-toggle';
        toggle.textContent = 'Показать';
        toggle.setAttribute('aria-label', 'Показать пароль');
        wrapper.appendChild(toggle);
        toggle.addEventListener('click', () => {
            const isHidden = input.type === 'password';
            input.type = isHidden ? 'text' : 'password';
            toggle.textContent = isHidden ? 'Скрыть' : 'Показать';
            toggle.setAttribute('aria-label', isHidden ? 'Скрыть пароль' : 'Показать пароль');
            input.focus();
        });
    });

    // Registration password requirements and confirmation.
    const password = $('#password');
    const confirmPassword = $('#confirmPassword');
    if (password && confirmPassword) {
        const strength = document.createElement('div');
        strength.className = 'password-strength';
        strength.setAttribute('aria-live', 'polite');
        const passwordField = password.closest('.form-field') || password.parentElement;
        if (passwordField) passwordField.appendChild(strength);

        const validatePassword = () => {
            const value = password.value;
            const checks = [
                value.length >= 16,
                /[A-ZА-Я]/.test(value),
                /\d/.test(value),
                /[^A-Za-zА-Яа-я0-9]/.test(value)
            ];
            const count = checks.filter(Boolean).length;
            const labels = ['Добавь 16 символов, заглавную букву и цифру', 'Слишком простой', 'Средний', 'Хороший', 'Сильный'];
            strength.textContent = value.length === 0 ? 'Надёжный пароль: от 16 символов, заглавная буква и цифра.' : labels[count];
            strength.dataset.level = String(count);
            confirmPassword.setCustomValidity(confirmPassword.value && confirmPassword.value !== value ? 'Пароли не совпадают.' : '');
        };
        const validateConfirmation = () => {
            confirmPassword.setCustomValidity(confirmPassword.value !== password.value ? 'Пароли не совпадают.' : '');
        };
        password.addEventListener('input', validatePassword);
        confirmPassword.addEventListener('input', validateConfirmation);
        validatePassword();
    }

    // Topic preview is plain text; user-supplied content is never interpreted as HTML.
    const topicContent = $('textarea[name="content"]');
    const topicForm = topicContent && topicContent.closest('form[action*="/topics"]');
    if (topicContent && topicForm && $('#title', topicForm)) {
        const previewToggle = document.createElement('button');
        previewToggle.type = 'button';
        previewToggle.className = 'button button-secondary button-small preview-toggle';
        previewToggle.textContent = 'Предпросмотр';
        topicContent.insertAdjacentElement('afterend', previewToggle);

        const preview = document.createElement('section');
        preview.className = 'topic-preview-panel';
        preview.hidden = true;
        preview.innerHTML = '<p class="eyebrow">ПРЕДПРОСМОТР</p><h3 class="topic-preview-title"></h3><p class="topic-preview-text"></p>';
        topicContent.closest('.form-field').appendChild(preview);

        const refreshPreview = () => {
            $('.topic-preview-title', preview).textContent = $('#title', topicForm).value.trim() || 'Заголовок темы';
            $('.topic-preview-text', preview).textContent = topicContent.value.trim() || 'Здесь появится текст темы.';
        };
        $('#title', topicForm).addEventListener('input', refreshPreview);
        topicContent.addEventListener('input', refreshPreview);
        previewToggle.addEventListener('click', () => {
            preview.hidden = !preview.hidden;
            previewToggle.textContent = preview.hidden ? 'Предпросмотр' : 'Скрыть предпросмотр';
            refreshPreview();
        });
    }

    // Share/copy link controls on a topic page.
    const topicArticle = $('.topic-article');
    if (topicArticle) {
        const tools = document.createElement('div');
        tools.className = 'topic-tools';
        const copyButton = document.createElement('button');
        copyButton.type = 'button';
        copyButton.className = 'button button-secondary button-small';
        copyButton.textContent = 'Скопировать ссылку';
        copyButton.addEventListener('click', async () => {
            try {
                await navigator.clipboard.writeText(window.location.href);
                copyButton.textContent = 'Ссылка скопирована';
            } catch (_) {
                const field = document.createElement('textarea');
                field.value = window.location.href;
                field.setAttribute('readonly', '');
                field.className = 'clipboard-fallback';
                document.body.appendChild(field);
                field.select();
                const copied = document.execCommand('copy');
                field.remove();
                copyButton.textContent = copied ? 'Ссылка скопирована' : 'Скопируй ссылку из адресной строки';
            }
            window.setTimeout(() => { copyButton.textContent = 'Скопировать ссылку'; }, 2200);
        });
        tools.appendChild(copyButton);
        const heading = $('h1', topicArticle);
        if (heading) heading.insertAdjacentElement('afterend', tools);
    }

    // Avoid accidental double submissions and show immediate feedback.
    $$('form').forEach(form => {
        if (form.classList.contains('search-form') || form.dataset.noLoading === 'true') return;
        form.addEventListener('submit', event => {
            if (event.defaultPrevented || !form.checkValidity()) return;
            if (form.dataset.submitting === 'true') {
                event.preventDefault();
                return;
            }
            form.dataset.submitting = 'true';
            const submit = $('button[type="submit"], input[type="submit"]', form);
            if (submit) {
                submit.disabled = true;
                submit.setAttribute('aria-busy', 'true');
                if (submit.tagName === 'BUTTON') {
                    submit.dataset.originalText = submit.textContent;
                    submit.textContent = 'Отправка…';
                }
            }
        });
    });

    // Press / to focus the first search input, except while typing in another field.
    document.addEventListener('keydown', event => {
        if (event.key !== '/' || event.ctrlKey || event.altKey || event.metaKey) return;
        const active = document.activeElement;
        if (active && /^(INPUT|TEXTAREA|SELECT)$/.test(active.tagName)) return;
        const search = $('input[type="search"]');
        if (search) {
            event.preventDefault();
            search.focus();
        }
    });
})();
